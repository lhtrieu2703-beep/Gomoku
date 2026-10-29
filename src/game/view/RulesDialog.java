package game.view;
import javax.swing.*;
import java.awt.*;
/** Rule text is supplied verbatim by Lang, with geometric stone examples. */
public final class RulesDialog extends LocalizedDialog {
    private final JTextArea body=new JTextArea();
    private final JButton close=new ThemedButton("",ThemedButton.Style.PRIMARY);
    public RulesDialog(MainFrame owner) {
        super(owner); JPanel content=new JPanel(new BorderLayout(0,12)); content.setBorder(BorderFactory.createEmptyBorder(18,24,18,24));
        JPanel examples=new JPanel(new GridLayout(2,1,0,8)); examples.setOpaque(false); examples.add(new Example(true)); examples.add(new Example(false));
        body.setEditable(false); body.setLineWrap(true); body.setWrapStyleWord(true); body.setFont(Theme.font(Font.PLAIN,15));
        JScrollPane scroll=new JScrollPane(body); scroll.setPreferredSize(new Dimension(590,320)); scroll.setBorder(BorderFactory.createEmptyBorder());
        content.add(examples,BorderLayout.NORTH); content.add(scroll); content.add(close,BorderLayout.SOUTH); setContentPane(content);
        close.addActionListener(e -> dispose()); finishLayout(close);
    }
    protected void refreshLanguage() {
        setTitle(Lang.rulesTitle()); StringBuilder text=new StringBuilder();
        for(int index=1;index<=6;index++) { if(index>1) text.append("\n\n"); text.append(Lang.rules("rules.l"+index)); }
        body.setText(text.toString()); body.setCaretPosition(0); close.setText(Lang.close()); repack();
    }
    public JTextArea getBody() { return body; } public JButton getCloseButton() { return close; }
    private static final class Example extends JComponent {
        private final boolean win;
        Example(boolean win) { this.win=win; setPreferredSize(new Dimension(590,50)); }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g=(Graphics2D)graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int x=6,y=10,cell=28; g.setColor(Theme.BOARD); g.fillRect(x,y,cell*7,cell);
            for(int i=0;i<7;i++) { g.setColor(Theme.WOOD_DARK); g.drawRect(x+i*cell,y,cell,cell);
                if(i==6&&win) continue;
                StonePainter.paint(g,x+i*cell+cell/2,y+cell/2,11,i==0||i==6?game.model.Piece.O:game.model.Piece.X);
            }
            g.setColor(win?Theme.SUCCESS:Theme.ERROR); g.setStroke(new BasicStroke(2.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            int sx=218; if(win) { g.drawLine(sx,23,sx+6,30); g.drawLine(sx+6,30,sx+17,15); }
            else { g.drawLine(sx+2,16,sx+15,29); g.drawLine(sx+15,16,sx+2,29); }
            g.setFont(Theme.font(Font.PLAIN,13)); g.setColor(Theme.CREAM); g.drawString(Lang.rules(win?"rules.winExample":"rules.noWinExample"),sx+27,28); g.dispose();
        }
    }
}
