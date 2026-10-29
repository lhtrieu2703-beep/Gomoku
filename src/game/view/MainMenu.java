package game.view;
import javax.swing.*;
import java.awt.*;
public final class MainMenu extends BackgroundPanel {
    private final ThemedButton profile=new ThemedButton("",ThemedButton.Style.SECONDARY),pvp=new ThemedButton("",ThemedButton.Style.SECONDARY),ai=new ThemedButton("",ThemedButton.Style.PRIMARY),lan=new ThemedButton("",ThemedButton.Style.SECONDARY);
    private final JButton rules=iconButton(UiIcon.Kind.BOOK),settings=iconButton(UiIcon.Kind.MENU);
    private final Logo logo=new Logo();
    public MainMenu(Runnable edit,Runnable playAi,Runnable playPvp,Runnable playLan,Runnable showRules,Runnable showSettings) {
        setLayout(null); for(Component c:new Component[]{profile,rules,settings,logo,ai,pvp,lan}) add(c);
        profile.addActionListener(e -> edit.run()); ai.addActionListener(e -> playAi.run()); pvp.addActionListener(e -> playPvp.run());
        lan.addActionListener(e -> playLan.run()); rules.addActionListener(e -> showRules.run()); settings.addActionListener(e -> showSettings.run());
    }
    public static JButton iconButton(UiIcon.Kind kind) {
        var b=new ThemedButton("",ThemedButton.Style.SECONDARY); b.setIcon(new UiIcon(kind,22)); b.setPreferredSize(new Dimension(40,40)); return b;
    }
    public void updateLanguage(PlayerProfile player) {
        profile.setText(player.displayName()); profile.setIcon(new AvatarIcon(player.avatarIndex(),player.displayName(),40));
        profile.setToolTipText(Lang.profile()); profile.getAccessibleContext().setAccessibleName(Lang.profile());
        ai.setText(Lang.pvAI()); pvp.setText(Lang.pvp()); lan.setText(Lang.online());
        rules.setToolTipText(Lang.rulesTitle()); settings.setToolTipText(Lang.settings());
        rules.getAccessibleContext().setAccessibleName(Lang.rulesTitle()); settings.getAccessibleContext().setAccessibleName(Lang.settings()); repaint();
    }
    @Override public void doLayout() {
        int w=getWidth(),h=getHeight(); profile.setBounds(20,18,330,54); settings.setBounds(w-60,24,40,40); rules.setBounds(w-108,24,40,40);
        logo.setBounds(50,(int)(h*.12),w-100,(int)(h*.20)); int bw=Math.min(510,w-80),x=(w-bw)/2,y=(int)(h*.40);
        ai.setBounds(x,y,bw,80); pvp.setBounds(x,y+100,bw,68); lan.setBounds(x,y+188,bw,68);
    }
    @Override protected void paintChildren(Graphics g) { super.paintChildren(g); new UiIcon(UiIcon.Kind.PENCIL,18).paintIcon(this,g,profile.getX()+profile.getWidth()-28,profile.getY()+18); }
    public JButton getProfileButton() { return profile; } public JButton getAiButton() { return ai; }
    public JButton getPvpButton() { return pvp; } public JButton getLanButton() { return lan; }
    public JButton getRulesButton() { return rules; } public JButton getSettingsButton() { return settings; }
}
