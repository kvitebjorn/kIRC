package kirc;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class ChannelPane extends JPanel
    {
        private final JTextArea textArea;
        
        public ChannelPane()
        {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            
            textArea = new JTextArea(24,49);
            textArea.setText("");
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            
            add(scrollPane);
        }
        
        public static JPanel getTitlePanel(final JTabbedPane pane, final JPanel panel, 
                                           final String title, final KIRC kirc       )
        {
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            titlePanel.setOpaque(false);
            JLabel titleLbl = new JLabel(title);
            titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            titlePanel.add(titleLbl);
            JButton closeButton = new JButton("x");
            closeButton.setSize(17, 17);
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusable(false);
            closeButton.setBorder(BorderFactory.createEtchedBorder());
            closeButton.setBorderPainted(false);
            closeButton.setRolloverEnabled(true);

            closeButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    int i = pane.indexOfComponent(panel);
                    pane.remove(panel);
                    pane.validate();
                    
                    try
                    {
                        if(i == 0)
                            kirc.sendQUIT();
                        else
                            kirc.sendPARTOneChannel(i);
                    }
                    catch(IOException ioException)
                    {
                        ioException.printStackTrace();
                    }
                
                    kirc.removeChannel(i);
                }
            });
            titlePanel.add(closeButton);

            return titlePanel;
        }
        
        public JTextArea getTextArea() 
        { 
            return textArea; 
        }
}