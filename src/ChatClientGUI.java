import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;

public class ChatClientGUI extends JFrame {
    private JTextPane messageArea;
    private JTextField textField;
    private ChatClient client;
    private JButton exitButton;
    private Map<String, Color> userColors;

    public ChatClientGUI() {
        super("Chat Application");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Style
        Color backgroundColor = new Color(240, 240, 240);
        Color buttonColor = new Color(75, 75, 75);
        Color textColor = new Color(50, 50, 50);
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);

        messageArea = new JTextPane();
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(textFont);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        textField = new JTextField();
        textField.setFont(textFont);
        textField.setForeground(textColor);
        textField.setBackground(backgroundColor);

        // Adds a prompt for username
        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Name Entry", JOptionPane.PLAIN_MESSAGE);
        this.setTitle("Chat Application - " + name); // Set window title to include username.
        
        textField.addActionListener(e -> {
            String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + name +": " + textField.getText();
            client.sendMessage(message);
            textField.setText("");
        });

        add(textField, BorderLayout.SOUTH);

        // Modify actionPerformed to include the username and time stamp
        textField.addActionListener(e -> {
            String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]" + name + ": " + textField.getText();
            client.sendMessage(message);
            textField.setText("");
        });

        // Adds exit button and styles
        exitButton = new JButton("Exit");
        exitButton.setFont(buttonFont);
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.BLACK);
        exitButton.addActionListener(i -> {
            // Sends out a message when a user exits the server
            String departureMessage = name + " has left the chat.";
            client.sendMessage(departureMessage);

            // Delay to ensure the message is sent before exiting
            try {
                Thread.sleep(1000); // Wait for 1 second
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            // Exits application
            System.exit(0);
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(exitButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        userColors = new HashMap<>(); // Initialize user colors map

        // Initializes and starts the ChatClient
        try {
            this.client = new ChatClient("127.0.0.1", 8090, this::onMessageReceived);
            client.startClient();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the server", "Connection error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Message received: " + message);  // Debug print
            String user = extractUserFromMessage(message);  // Extract user from the message
            Color color = userColors.computeIfAbsent(user, k -> generateRandomColor());  // Get or generate color for user
            appendColoredText(message + "\n", color);  // Append colored message to the text area
        });
    }
    
    private String extractUserFromMessage(String message) {
        int startIndex = message.indexOf("]") + 2;
        int endIndex = message.indexOf(":");
        return message.substring(startIndex, endIndex).trim();
    }

    private Color generateRandomColor() {
        return new Color((int)(Math.random() * 0x1000000));
    }

    private void appendColoredText(String message, Color color) {
        StyledDocument doc = messageArea.getStyledDocument();
        Style style = messageArea.addStyle("UserStyle", null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), message, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private SimpleAttributeSet ColorAttributes(Color color) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        return attributes;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);}
        );
    }
}