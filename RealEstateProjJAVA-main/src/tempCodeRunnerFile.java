import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new RealEstateFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768); // Updated size
        frame.setVisible(true);
    }
}
