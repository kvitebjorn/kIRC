package kirc;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ChannelPane extends JPanel
    {
        private final JTextArea textArea;
        private final JTabbedPane _pane;
        private final KIRC _kirc;
        
        public ChannelPane(JTabbedPane pane, KIRC kirc)
        {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            _pane = pane;
            _kirc = kirc;
            textArea = new JTextArea(5,20);
            textArea.setText("");
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            add(scrollPane);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            JButton closeButton = new JButton("x");

            closeButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    int i = _pane.getSelectedIndex();
                    if (i != -1) 
                        _pane.remove(i);
                    //remove channel from kirc channels
                    _kirc.removeChannel(i);
                }
            });
            
            add(closeButton);
        }
        
        public JTextArea getTextArea() 
        { 
            return textArea; 
        }
}