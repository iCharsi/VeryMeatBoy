package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/11/14.
 */
interface Strings {
    String DEATH_MESSAGE = "DEAD, Press R to retry or Esc to return to menu";
    String VICTORY_MESSAGE = "VICTORY, Press Enter for the next level or R to replay this level";

    String MENU_NEW_GAME = "New game";
    String MENU_CONTINUE = "Continue";
    String MENU_EDITOR = "Editor";
    String MENU_EXIT = "Exit";
    String MENU_SAVE_LEVEL_SET = "Save level set";
    String MENU_LOAD_LEVEL_SET = "Load level set";
    String MENU_CREATE_LEVEL = "Create new level";
    String MENU_EDIT_LEVEL = "Edit level";
    String MENU_SWITCH_LEVELS = "Select level AFTER which you want to put this level and press Enter";

    String FRAME_SKIP_ON = "Frame skip is ENABLED";
    String FRAME_SKIP_OFF = "Frame skip is DISABLED";

    String EDITOR_INCORRECT_VALUES = "Some values were incorrect, no changes were made";
    String EDITOR_PORTAL_DESTINATION = "Click on the destination portal or click anywhere else to cancel";
    String EDITOR_PORTAL_TO_ITSELF = "Portal cannot lead to itself!";

    String LEVEL_SET_SAVE_ERROR = "ERROR! Couldn't save level set to selected file";
    String LEVEL_SET_LOAD_ERROR = "ERROR! Couldn't read level set from selected file";

    String LEVEL_SET_SAVE_SUCCESS = "Success! Level set saved";
    String LEVEL_SET_LOAD_SUCCESS = "Success! Level set loaded";

    String NEED_HELP = "Press F1 if you need help!";
}
