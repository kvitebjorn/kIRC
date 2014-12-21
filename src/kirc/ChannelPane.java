package kirc;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChannelPane extends JPanel
    {
        private final JTextArea textArea;
        
        public ChannelPane()
        {
            textArea = new JTextArea(5,20);
            textArea.setText("");
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            add(scrollPane);
        }
        
        public JTextArea getTextArea() { return textArea; }
    }