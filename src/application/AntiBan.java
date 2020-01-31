package application;

import static org.osbot.rs07.script.MethodProvider.random;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;

public class AntiBan {

    private final Script script;

    private int minActionBound = 0; // minimum time between actions
    private int maxActionBound = 0; // maximum time between actions
    private long nextExecuteTime;

    private List<Action> defaultActions = Arrays.asList(Action.values());

    List<Action> actionList = new ArrayList<>();

    /**
     * Creates the object with all default actions to be performed.
     *
     * @param script script reference
     * @param minActionBound minimum delay between anti ban actions
     * @param maxActionBound maximum delay between anti ban actions
     */
    public AntiBan(Script script, int minActionBound, int maxActionBound) {
        this(script, minActionBound, maxActionBound, Arrays.asList(Action.values()));
    }

    /**
     * Actions must be added manually.
     *
     * @param script script reference
     * @param minActionBound minimum delay between anti ban actions
     * @param maxActionBound maximum delay between anti ban actions
     * @param actions the actions that should be performed
     */
    public AntiBan(Script script, int minActionBound, int maxActionBound, List<Action> actions) {
        this.script = script;
        this.minActionBound = minActionBound;
        this.maxActionBound = maxActionBound;
        nextExecuteTime = System.currentTimeMillis() + random(minActionBound, maxActionBound);
        actionList = actions;
    }

    /**
     * @return true if it's time to perform an action
     */
    public boolean shouldExecute() {
        return (System.currentTimeMillis() > nextExecuteTime);
    }

    public long getNextExecuteTime() {
        return nextExecuteTime;
    }

    /**
     * Performs a random action from the action list based off of the weighted
     * values.
     *
     * How it works: Creates an array the size of the cumulative weight
     * of the action list and then loops through the action list assigning each
     * index the value of the enum value. Then chooses a random number
     * 0-arrayLength to get the action to perform.
     */
    public void execute() {
        if (actionList.size() > 0) {
            // calcualte the cumulative weight of the action list
            int cumulativeWeight = 0;
            for (Action action : actionList) {
                cumulativeWeight += action.getWeight();
            }

            // for every action in the action list, add one slot to wheel per weight
            int[] wheel = new int[cumulativeWeight];
            int index = 0; // keep track of last index

            for (int i = 0; i < actionList.size(); i++) {
                int numSlots = actionList.get(i).getWeight();
                while (numSlots-- > 0) {
                    wheel[index++] = actionList.get(i).ordinal();
                }
            }

            // get an action from a random slot
            int actionOrdinal = wheel[random(0, wheel.length) - 1];
            Action action = Action.values()[actionOrdinal];

            // perform the action
            switch (action) {
                case MOVE_MOUSE:
                    script.log("[ANTI BAN:] Moving mouse.");
                    moveMouseRandomly();
                    break;
                case ROTATE_CAMERA:
                    script.log("[ANTI BAN:] Rotating camera.");
                    rotateCameraRandomly();
                    break;
                case RIGHT_CLICK_RANDOM_OBJECT:
                    script.log("[ANTI BAN:] Right-clicking an object");
                    rightClickRandomObject();
                    break;
                case CHECK_WC_EXP:
                    script.log("[ANTI BAN:] Checking WC EXP");
                    checkExp(Skill.WOODCUTTING);
                    break;
                case CHECK_FM_EXP:
                    script.log("[ANTI BAN:] Checking FM EXP");
                    checkExp(Skill.FIREMAKING);
                    break;
            }

            // set the next execute time
            nextExecuteTime = System.currentTimeMillis() + random(minActionBound, maxActionBound);
        }
    }

    @SuppressWarnings("static-access")
	private void checkExp(Skill skill) {
    	script.getTabs().open(Tab.SKILLS);
    	try {
			script.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
    	RS2Widget widget = script.getWidgets().get(320, skill.getChildId());
		if(widget == null)
			return;
		Rectangle rect = widget.getBounds();
		int x = random(rect.x, rect.x+rect.width);
		int y = random(rect.y, rect.y+rect.height);
		script.getMouse().move(x,y);
		try {
			script.sleep(random(2000, 4000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		script.getTabs().open(Tab.INVENTORY);
	}

	public void addAction(Action a) {
        actionList.add(a);
    }

    /**
     * @param a the action to add
     * @param weight likeliness of this action to be performed
     */
    public void addAction(Action a, int weight) {
        Action action = a;
        action.setWeight(weight);
        actionList.add(action);
    }

    /**
     * Adds an occurrence of all Actions to the action list
     */
    public void addAllActions() {
        actionList = defaultActions;
    }

    public void clearActions() {
        actionList.clear();
    }

    /**
     * Each action is weighted. Meaning the higher the weight, the more likely
     * that action is to be performed. If two actions have the same weight, they
     * are equally likely to be performed.<br><br>
     *
     *
     * Default weights<br>
     * ------------------------<br>
     * Move mouse: 3<br>
     * Rotate camera: 7<br>
     * Right click object: 1
     */
    public enum Action {
        MOVE_MOUSE(3),
        ROTATE_CAMERA(7),
        RIGHT_CLICK_RANDOM_OBJECT(1),
        CHECK_FM_EXP(20),
        CHECK_WC_EXP(20);

        int weight;

        Action(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    /////////////////////////////
    //     Helper methods      //
    /////////////////////////////
    private void moveMouse() {
        script.getMouse().move(random(43, 538), random(47, 396));
    }

    /////////////////////////////
    //  Methods for execution  //
    /////////////////////////////
    public void moveMouseRandomly() {
        script.getMouse().move(random(43, 538), random(47, 396));
    }

    public void rotateCameraRandomly() {
        script.getCamera().movePitch(random(script.getCamera().getLowestPitchAngle(), 67));
        script.getCamera().moveYaw(random(0, 359));
    }

    /**
     * Right clicks a random visible object and then moves mouse to close the
     * menu.
     */
    public void rightClickRandomObject() {
        List<RS2Object> visibleObjs = script.getObjects().getAll().stream().filter(o -> o.isVisible()).collect(Collectors.toList());

        // select a random object
        int index = random(0, visibleObjs.size() - 1);
        RS2Object obj = visibleObjs.get(index);

        if (obj != null) {
            // hover the object and right click
            obj.hover();
            script.getMouse().click(true);

            // while the menu is still open, move the mouse to a new location
            while (script.getMenuAPI().isOpen()) {
                moveMouse();
            }
        }
    }

}
