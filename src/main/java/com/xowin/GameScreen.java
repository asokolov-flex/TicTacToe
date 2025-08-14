package com.xowin;

public interface GameScreen {
    void setScreenSize(int var1, int var2);

    void setCellColor(int var1, int var2, Color var3);

    void setCellTextSize(int var1, int var2, int var3);

    void setCellValue(int var1, int var2, String var3);

    String getCellValue(int var1, int var2);

    void setCellTextColor(int var1, int var2, Color var3);

    void setCellValueEx(int var1, int var2, Color var3, String var4);

    void setCellValueEx(int var1, int var2, Color var3, String var4, Color var5);

    void showMessageDialog(Color var1, String var2, Color var3, int var4);

    void initialize();

    void onMouseLeftClick(int var1, int var2);

    void onMouseRightClick(int var1, int var2);

    void onKeyPress(Key var1);

    void onKeyReleased(Key var1);

    void onTurn(int var1);
}
