package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/6/14.
 */
public class Menu {
    private final int itemCount;
    private int selectedItem;
    private int secondaryItem = -1;
    private final String[] caption;
    private final String[] action;
    private final Menu[] subMenu;
    private final Menu parent;

    public int getItemCount() {
        return itemCount;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public int getSecondaryItem() {
        return secondaryItem;
    }

    public void selectSecondaryItem() {
        secondaryItem = selectedItem;
    }

    public void deselectSecondaryItem() {
        secondaryItem = -1;
    }

    public void goUp() {
        selectedItem = (selectedItem + itemCount - 1) % itemCount;
    }

    public void goDown() {
        selectedItem = (selectedItem + 1) % itemCount;
    }

    public Menu goInside(int index) {
        if (index >= 0 && index < itemCount && subMenu[index] != null) {
            subMenu[index].selectedItem = 0;
            return subMenu[index];
        }
        return this;
    }

    public String getCaption(int index) {
        return caption[index];
    }

    public String getAction(int index) {
        return action[index];
    }

    public Menu getParent() {
        return parent;
    }

    Menu(int itemCount, Menu parent) {
        this.itemCount = itemCount;
        selectedItem = 0;
        caption = new String[itemCount];
        action = new String[itemCount];
        subMenu = new Menu[itemCount];
        this.parent = parent;
    }

    public Menu(int levelCount) {
        itemCount = 4;
        selectedItem = 0;
        caption = new String[itemCount];
        caption[0] = Strings.MENU_NEW_GAME;
        caption[1] = Strings.MENU_CONTINUE;
        caption[2] = Strings.MENU_EDITOR;
        caption[3] = Strings.MENU_EXIT;
        action = new String[itemCount];
        action[0] = "new_game";
        action[1] = "continue";
        action[3] = "exit";
        subMenu = new Menu[itemCount];
        subMenu[2] = new Menu(levelCount + 3, this);
        subMenu[2].caption[0] = Strings.MENU_SAVE_LEVEL_SET;
        subMenu[2].action[0] = "save_levels";
        subMenu[2].caption[1] = Strings.MENU_LOAD_LEVEL_SET;
        subMenu[2].action[1] = "load_levels";
        for (int i = 0; i < levelCount; ++i) {
            subMenu[2].caption[i + 2] = Strings.MENU_EDIT_LEVEL + " " + i;
            subMenu[2].action[i + 2] = "editor" + i;
        }
        subMenu[2].caption[levelCount + 2] = Strings.MENU_CREATE_LEVEL;
        subMenu[2].action[levelCount + 2] = "editor" + levelCount;
        this.parent = null;
    }
}
