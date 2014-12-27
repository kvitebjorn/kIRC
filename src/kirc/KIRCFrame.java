package kirc;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

public final class KIRCFrame extends javax.swing.JFrame
{
    private final KIRC kirc;
    
    /**
     * Creates new form KIRCFrame
     */
    public KIRCFrame()
    {
        initComponents();
        
        kirc = new KIRC(this, "irc.freenode.org", "k-test");
        
        userNameLabel.setText(kirc.getNick() + " λ");
        
        DefaultCaret caret = (DefaultCaret) channelBanner.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        kirc.runClient();
    }
    
    public KIRCFrame getKIRCFrame()
    {
        return this;
    }
    
    public void addTab(final String s)
    {
        JPanel panel = (JPanel)makeChannelPanel();
        channelPane.addTab(s, panel);
        channelPane.setTabComponentAt(channelPane.indexOfComponent(panel),
                ChannelPane.getTitlePanel(channelPane, panel, s, kirc));
    }
    
    public void displayMessage(final String messageToDisplay, final int channelIndex)
    {
        SwingUtilities.invokeLater(() ->
        {
            ChannelPane channelArea = null;
            if(channelIndex < channelPane.getTabCount() && channelIndex > -1)
                channelArea = (ChannelPane)channelPane.getComponentAt(channelIndex);

            if(channelArea != null)
                channelArea.getTextArea().append(messageToDisplay);
        });
    }
    
    public void setTextFieldEditable(final boolean editable)
    {
        SwingUtilities.invokeLater(() ->
        {
            enterField.setEditable(editable);
        });
    }
    
    protected JComponent makeChannelPanel() 
    {
        ChannelPane channelArea = new ChannelPane();
        return channelArea;
    }
    
    public void setFocusOnChannel(final int i)
    {
        if(i < channelPane.getTabCount())
            channelPane.setSelectedIndex(i);
    }
    
    public int getChannelFocus()
    {
        return channelPane.getSelectedIndex();
    }
    
    public int getChannelPaneSize()
    {
        return channelPane.getTabCount();
    }
    
    public void setUserList(String[] users)
    {
        userList.removeAll();
        userList.setListData(users);
        //TODO: wait for the end of user list command when setting user list
    }
    
    public void setBannerNow(final String msg)
    {
        channelBanner.setText(msg);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        userListScrollPane = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        channelPane = new javax.swing.JTabbedPane();
        enterField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        channelBannerScrollPane = new javax.swing.JScrollPane();
        channelBanner = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(705, 570));
        setResizable(false);

        userListScrollPane.setViewportView(userList);

        channelPane.setBackground(new java.awt.Color(204, 204, 204));
        channelPane.setBorder(null);
        channelPane.setMaximumSize(new java.awt.Dimension(100, 100));
        channelPane.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                channelPaneStateChanged(evt);
            }
        });

        enterField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                enterFieldActionPerformed(evt);
            }
        });

        userNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        userNameLabel.setText("username");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Users");

        channelBannerScrollPane.setBorder(null);
        channelBannerScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        channelBannerScrollPane.setHorizontalScrollBar(null);
        channelBannerScrollPane.setPreferredSize(new java.awt.Dimension(558, 17));

        channelBanner.setEditable(false);
        channelBanner.setColumns(1);
        channelBanner.setLineWrap(true);
        channelBanner.setRows(2);
        channelBanner.setBorder(null);
        channelBannerScrollPane.setViewportView(channelBanner);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(channelBannerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addComponent(channelBannerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userListScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(channelPane, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 549, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enterField, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(channelPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userNameLabel))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enterFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_enterFieldActionPerformed
    {//GEN-HEADEREND:event_enterFieldActionPerformed
        kirc.enterFieldFired(enterField.getText());
        enterField.setText("");
    }//GEN-LAST:event_enterFieldActionPerformed

    private void channelPaneStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_channelPaneStateChanged
    {//GEN-HEADEREND:event_channelPaneStateChanged
        int channelIndex = channelPane.getSelectedIndex();
        Channel ch = kirc.getChannel(channelIndex);
        if(ch != null)
        {
            ArrayList<String> userList = ch.getUsersList();
            setUserList(userList.toArray(new String[userList.size()]));
            
            if(ch.getBanner().equals(""))
            {                
                channelBanner.setText("kIRC :: " + ch.getChannelName());
            }
            else
            {
                setBannerNow(ch.getBanner());
            }
        }
    }//GEN-LAST:event_channelPaneStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        KIRCFrame frame = new KIRCFrame();
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(KIRCFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(KIRCFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(KIRCFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(KIRCFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                javax.swing.JFrame frame = new javax.swing.JFrame("kIRC");
                frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(new KIRCFrame());
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea channelBanner;
    private javax.swing.JScrollPane channelBannerScrollPane;
    private javax.swing.JTabbedPane channelPane;
    private javax.swing.JTextField enterField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JList userList;
    private javax.swing.JScrollPane userListScrollPane;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables
}
