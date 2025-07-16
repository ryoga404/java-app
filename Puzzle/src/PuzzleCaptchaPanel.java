import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PuzzleCaptchaPanel extends JPanel {
    private BufferedImage originalBackground;
    private BufferedImage background;
    private BufferedImage backgroundWithHole;
    private BufferedImage piece;

    private final int pieceSize = 50;
    private int pieceY;
    private int correctX;
    private int dragX;

    private JSlider slider;

    private final int maxWidth = 320;

    public PuzzleCaptchaPanel() {
        loadAndResizeImage();
        if (background == null) return;

        prepareCaptcha();

        int sliderMax = backgroundWithHole.getWidth() - pieceSize;
        slider = new JSlider(0, sliderMax);
        slider.setValue(0);
        slider.addChangeListener((ChangeEvent e) -> {
            dragX = slider.getValue();
            repaint();
        });

        setPreferredSize(new Dimension(backgroundWithHole.getWidth(), backgroundWithHole.getHeight() + 50));
        setLayout(new BorderLayout());
        add(slider, BorderLayout.SOUTH);
    }

    private void loadAndResizeImage() {
        try {
            originalBackground = ImageIO.read(new File("resources/img0.jpg"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "背景画像の読み込みに失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int w = originalBackground.getWidth();
        int h = originalBackground.getHeight();

        if (w > maxWidth) {
            double scale = (double) maxWidth / w;
            int newW = maxWidth;
            int newH = (int) (h * scale);

            background = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = background.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalBackground, 0, 0, newW, newH, null);
            g2d.dispose();
        } else {
            background = originalBackground;
        }
    }

    private void prepareCaptcha() {
        int w = background.getWidth();
        int h = background.getHeight();

        pieceY = h / 2 - pieceSize / 2;
        correctX = 50 + (int) (Math.random() * (w - 100 - pieceSize));

        piece = background.getSubimage(correctX, pieceY, pieceSize, pieceSize);

        backgroundWithHole = new BufferedImage(w, h, background.getType());
        Graphics2D g2d = backgroundWithHole.createGraphics();
        g2d.drawImage(background, 0, 0, null);
        g2d.setColor(new Color(180, 180, 180));
        g2d.fillRect(correctX, pieceY, pieceSize, pieceSize);
        g2d.dispose();

        dragX = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundWithHole != null) g.drawImage(backgroundWithHole, 0, 0, null);
        if (piece != null) g.drawImage(piece, dragX, pieceY, null);
    }

    // 認証判定メソッド
    public boolean checkPuzzlePosition() {
        return Math.abs(dragX - correctX) <= 10;
    }
}
