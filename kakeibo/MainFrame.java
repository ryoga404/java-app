import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public MainFrame() {
        setTitle("家計簿アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(new LoginPanel(this), "login");
        //cardPanel.add(new RegiserPanel(this), "register");

        add(cardPanel);
        showPanel("login");
    }

    public void showPanel(String name) {
        cardLayout.show(cardPanel, name);
    }
}
