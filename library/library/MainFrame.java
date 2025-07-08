//package jp.ac.kcs.swing.library;
package library;

import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public MainFrame() {
        setTitle("図書館システム");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // すべての機能パネルを登録
        cardPanel.add(new TopPanel(this), "TOP");
        cardPanel.add(new BookPanel(this), "BOOK");
        cardPanel.add(new MemberPanel(this), "MEMBER");
        cardPanel.add(new LendPanel(this), "LEND");
        cardPanel.add(new ReturnPanel(this), "RETURN");
        cardPanel.add(new ListPanel(this), "LIST");

        add(cardPanel);
        setVisible(true);
    }

    public void showPanel(String name) {
        cardLayout.show(cardPanel, name);
    }
}
