/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.game;

import javax.swing.*;
import java.awt.*;//abstract window toolkit
import java.awt.event.*;
import java.util.*;

public class Game extends JFrame {//inheritance and encapsulation

    static final int MENU=0, NAMING=1, PLAYING=2, OVER=3, ROUND_END=4;
    static final Color SKY1=new Color(15,15,35), SKY2=new Color(25,40,70);
    static final Color P1C=new Color(70,160,255), P1D=new Color(30,100,200);
    static final Color P2C=new Color(255,80,80),  P2D=new Color(180,20,20);
    static final Color GND=new Color(34,85,34),   GUN=new Color(80,80,80);
    static final Color BULL=new Color(255,230,50);
    static final int   FLOOR=300, W=800, H=480;

    int state=MENU;
    String p1Name="Player 1", p2Name="Player 2";

    int p1Wins=0, p2Wins=0, currentRound=1;
    static final int MAX_ROUNDS=3;
    static final int WINS_NEEDED=2;
    String roundWinner="";
    Color roundWinCol=Color.WHITE;
    int roundEndTimer=0;
    boolean roundEndPanelShown=false;

    // Winner overlay variables — always drawn on top until dismissed
    boolean showWinnerOverlay=false;
    String overlayText="";
    Color overlayColor=Color.WHITE;

    int   p1x=120, p1y=FLOOR, p2x=620, p2y=FLOOR;
    int   p1hp=100, p2hp=100;
    int   p1vy=0, p2vy=0;
    boolean p1facingRight=true, p2facingRight=false;
    boolean p1hit=false, p2hit=false;
    int   p1walk=0, p2walk=0;

    boolean p1shield=false, p2shield=false;
    int p1shieldTimer=0, p2shieldTimer=0;
    int p1shieldCooldown=0, p2shieldCooldown=0;
    static final int SHIELD_DURATION=120, SHIELD_COOLDOWN=300;

    boolean bActive=false;
    int bx,by,bDir,bOwner;

    boolean bombActive=false;
    int bombX,bombY,bombVx,bombVy,bombOwner,bombTimer=0;
    boolean exploding=false;
    int explodeX,explodeY,explodeTimer=0;
    int p1BombCooldown=0, p2BombCooldown=0;

    Set<Integer> keys=new HashSet<>();
    int[] sx=new int[55], sy=new int[55], ss=new int[55];

    JTextField f1,f2;
    Arena arena;

    public Game(){
        setTitle("⚔ Battle Arena");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        for(int i=0;i<sx.length;i++){
            sx[i]=(int)(Math.random()*W);
            sy[i]=(int)(Math.random()*260);
            ss[i]=(int)(Math.random()*3)+1;
        }
        arena=new Arena();
        add(arena);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        new javax.swing.Timer(16,e->tick()).start();//abstraction
    }

    void tick(){
        if(state==ROUND_END){
            roundEndTimer++;
            if(roundEndTimer>90 && !roundEndPanelShown){
                roundEndPanelShown=true;
                showRoundEndPanel();
            }
            arena.repaint();
            return;
        }
        if(state!=PLAYING){
            arena.repaint();
            return;
        }

        if(keys.contains(KeyEvent.VK_A)){p1x-=4;p1facingRight=false;p1walk++;}
        if(keys.contains(KeyEvent.VK_D)){p1x+=4;p1facingRight=true; p1walk++;}
        if(keys.contains(KeyEvent.VK_W)&&p1y>=FLOOR) p1vy=-16;
        if(keys.contains(KeyEvent.VK_LEFT)){p2x-=4;p2facingRight=false;p2walk++;}
        if(keys.contains(KeyEvent.VK_RIGHT)){p2x+=4;p2facingRight=true; p2walk++;}
        if(keys.contains(KeyEvent.VK_UP)&&p2y>=FLOOR) p2vy=-16;

        p1vy+=1; p1y+=p1vy;
        if(p1y>FLOOR){p1y=FLOOR;p1vy=0;}
        p2vy+=1; p2y+=p2vy;
        if(p2y>FLOOR){p2y=FLOOR;p2vy=0;}
        p1x=Math.max(30,Math.min(W-80,p1x));
        p2x=Math.max(30,Math.min(W-80,p2x));

        if(p1shield){p1shieldTimer--;
            if(p1shieldTimer<=0){p1shield=false;p1shieldCooldown=SHIELD_COOLDOWN;}}
        if(p2shield){p2shieldTimer--;
            if(p2shieldTimer<=0){p2shield=false;p2shieldCooldown=SHIELD_COOLDOWN;}}
        if(p1shieldCooldown>0) p1shieldCooldown--;
        if(p2shieldCooldown>0) p2shieldCooldown--;
        if(p1BombCooldown>0) p1BombCooldown--;
        if(p2BombCooldown>0) p2BombCooldown--;

        if(bActive){
            bx+=bDir*18;
            if(bOwner==1&&!p2shield&&Math.abs(bx-p2x)<35&&Math.abs(by-p2y)<60){
                bActive=false;
                p2hp=Math.max(0,p2hp-(8+(int)(Math.random()*12)));
                p2hit=true; scheduleHitReset();
            } else if(bOwner==2&&!p1shield&&Math.abs(bx-p1x)<35&&Math.abs(by-p1y)<60){
                bActive=false;
                p1hp=Math.max(0,p1hp-(8+(int)(Math.random()*12)));
                p1hit=true; scheduleHitReset();
            }
            if(bOwner==1&&p2shield&&Math.abs(bx-p2x)<40&&Math.abs(by-p2y)<65) bActive=false;
            if(bOwner==2&&p1shield&&Math.abs(bx-p1x)<40&&Math.abs(by-p1y)<65) bActive=false;
            if(bx<0||bx>W) bActive=false;
        }

        if(bombActive){
            bombX+=bombVx;
            bombVy+=1; bombY+=bombVy;
            bombTimer++;
            if(bombY>=FLOOR+50){bombY=FLOOR+50;bombVy=(int)(-bombVy*0.5);bombVx=(int)(bombVx*0.85);}
            if(bombTimer>120||(bombY>=FLOOR+48&&Math.abs(bombVy)<3&&bombTimer>30)) triggerExplosion();
        }
        if(exploding){
            explodeTimer++;
            if(explodeTimer==10) checkExplosionDamage();
            if(explodeTimer>40) exploding=false;
        }
        arena.repaint();
    }

    void scheduleHitReset(){
        new javax.swing.Timer(300,e->{
            p1hit=false; p2hit=false;
            if(state==PLAYING){
                if(p1hp<=0)      endRound(p2Name, P2C);
                else if(p2hp<=0) endRound(p1Name, P1C);
            }
        }){{setRepeats(false);}}.start();
    }

    void triggerExplosion(){
        bombActive=false; exploding=true;
        explodeX=bombX; explodeY=bombY;
        explodeTimer=0;
    }

    void checkExplosionDamage(){
        int radius=90; boolean anyHit=false;
        if(bombOwner!=1&&!p1shield&&Math.abs(explodeX-p1x)<radius&&Math.abs(explodeY-p1y)<radius){
            p1hp=Math.max(0,p1hp-(15+(int)(Math.random()*20)));
            p1hit=true; anyHit=true;
        }
        if(bombOwner!=2&&!p2shield&&Math.abs(explodeX-p2x)<radius&&Math.abs(explodeY-p2y)<radius){
            p2hp=Math.max(0,p2hp-(15+(int)(Math.random()*20)));
            p2hit=true; anyHit=true;
        }
        if(anyHit) scheduleHitReset();
    }

    void shoot(int who){
        if(bActive||state!=PLAYING) return;
        if(who==1){bx=p1x+30;by=p1y-40;bDir=p1facingRight?1:-1;}
        else      {bx=p2x+30;by=p2y-40;bDir=p2facingRight?1:-1;}
        bOwner=who; bActive=true;
    }

    void throwBomb(int who){
        if(bombActive||exploding||state!=PLAYING) return;
        if(who==1&&p1BombCooldown>0) return;
        if(who==2&&p2BombCooldown>0) return;
        if(who==1){bombX=p1x+30;bombY=p1y-40;bombVx=p1facingRight?10:-10;p1BombCooldown=200;}
        else      {bombX=p2x+30;bombY=p2y-40;bombVx=p2facingRight?10:-10;p2BombCooldown=200;}
        bombVy=-14; bombOwner=who; bombActive=true; bombTimer=0;
    }

    void activateShield(int who){
        if(state!=PLAYING) return;
        if(who==1&&!p1shield&&p1shieldCooldown<=0){p1shield=true;p1shieldTimer=SHIELD_DURATION;}
        if(who==2&&!p2shield&&p2shieldCooldown<=0){p2shield=true;p2shieldTimer=SHIELD_DURATION;}
    }

    void endRound(String winner, Color col){
        if(state==ROUND_END||state==OVER) return;
        state=ROUND_END;
        roundWinner=winner;
        roundWinCol=col;
        roundEndTimer=0;
        roundEndPanelShown=false;
        if(winner.equals(p1Name)) p1Wins++; else p2Wins++;

        // Set overlay so winner name is always visible
        showWinnerOverlay=true;
        overlayText=winner+" wins the round!";
        overlayColor=col;

        arena.repaint();
    }

    void showRoundEndPanel(){
        if(state!=ROUND_END) return;
        state=OVER;
        showWinnerOverlay=false; // hide overlay when panel appears
        arena.removeAll();

        boolean matchOver=(p1Wins>=WINS_NEEDED||p2Wins>=WINS_NEEDED);
        JPanel over=darkPanel(220,130,360,220);

        if(matchOver){
            // Big winner label
            JLabel trophy=new JLabel("🏆",SwingConstants.CENTER);
            trophy.setFont(new Font("Arial",Font.PLAIN,36));
            trophy.setBounds(0,8,360,44); over.add(trophy);

            JLabel lbl=new JLabel(roundWinner+" Wins the Match!",SwingConstants.CENTER);
            lbl.setForeground(roundWinCol);
            lbl.setFont(new Font("Arial",Font.BOLD,18));
            lbl.setBounds(0,52,360,28); over.add(lbl);

            JLabel sc=new JLabel(p1Name+" "+p1Wins+" — "+p2Wins+" "+p2Name,SwingConstants.CENTER);
            sc.setForeground(new Color(200,220,255));
            sc.setFont(new Font("Arial",Font.BOLD,14));
            sc.setBounds(0,84,360,22); over.add(sc);

            JLabel bot=new JLabel("Best of "+MAX_ROUNDS+" Complete!",SwingConstants.CENTER);
            bot.setForeground(new Color(255,220,80));
            bot.setFont(new Font("Arial",Font.BOLD,13));
            bot.setBounds(0,108,360,22); over.add(bot);

            over.add(btn("▶ New Match",55,148,130,36,new Color(50,150,50),e->{
                p1Wins=0;p2Wins=0;currentRound=1;
                showWinnerOverlay=false;
                arena.removeAll();arena.showNameInput();
            }));
            over.add(btn("Menu",220,148,90,36,new Color(60,60,160),e->{
                p1Wins=0;p2Wins=0;currentRound=1;
                showWinnerOverlay=false;
                arena.removeAll();arena.showMenu();
            }));

        } else {
            currentRound++;

            JLabel lbl=new JLabel("Round "+(currentRound-1)+" Winner:",SwingConstants.CENTER);
            lbl.setForeground(new Color(200,220,255));
            lbl.setFont(new Font("Arial",Font.BOLD,14));
            lbl.setBounds(0,12,360,22); over.add(lbl);

            // Big winner name — always visible
            JLabel wname=new JLabel(roundWinner,SwingConstants.CENTER);
            wname.setForeground(roundWinCol);
            wname.setFont(new Font("Arial",Font.BOLD,28));
            wname.setBounds(0,36,360,36); over.add(wname);

            JLabel sc=new JLabel("Score:  "+p1Name+" "+p1Wins+"  —  "+p2Wins+"  "+p2Name,SwingConstants.CENTER);
            sc.setForeground(new Color(200,220,255));
            sc.setFont(new Font("Arial",Font.BOLD,13));
            sc.setBounds(0,78,360,22); over.add(sc);

            JLabel nxt=new JLabel("Round "+currentRound+" of "+MAX_ROUNDS,SwingConstants.CENTER);
            nxt.setForeground(new Color(255,220,80));
            nxt.setFont(new Font("Arial",Font.BOLD,13));
            nxt.setBounds(0,102,360,22); over.add(nxt);

            over.add(btn("▶ Next Round",100,145,160,38,new Color(180,50,50),e->{
                showWinnerOverlay=false;
                arena.removeAll();
                resetRound();
                state=PLAYING;
                arena.revalidate();
                arena.repaint();
                arena.requestFocusInWindow();
            }));
        }
        arena.add(over);
        arena.revalidate();
        arena.repaint();
    }

    void startGame(){
        p1Name=f1.getText().trim().isEmpty()?"Player 1":f1.getText().trim();
        p2Name=f2.getText().trim().isEmpty()?"Player 2":f2.getText().trim();
        p1Wins=0; p2Wins=0; currentRound=1;
        resetRound();
        state=PLAYING;
        arena.removeAll();
        arena.revalidate();
        arena.repaint();
        arena.requestFocusInWindow();
    }

    void resetRound(){
        p1x=120;p2x=620;p1y=FLOOR;p2y=FLOOR;
        p1hp=100;p2hp=100;
        p1vy=0;p2vy=0;
        bActive=false;p1hit=false;p2hit=false;
        p1facingRight=true;p2facingRight=false;
        bombActive=false;exploding=false;
        bombTimer=0;explodeTimer=0;
        p1BombCooldown=0;p2BombCooldown=0;
        p1shield=false;p2shield=false;
        p1shieldTimer=0;p2shieldTimer=0;
        p1shieldCooldown=0;p2shieldCooldown=0;
        roundEndPanelShown=false;
        showWinnerOverlay=false;
    }

    JPanel darkPanel(int x,int y,int w,int h){
        JPanel p=new JPanel(null); p.setBounds(x,y,w,h);
        p.setBackground(new Color(15,20,50,230));
        p.setBorder(BorderFactory.createLineBorder(new Color(80,120,200),2));
        return p;
    }

    JButton btn(String t,int x,int y,int w,int h,Color bg,ActionListener al){
        JButton b=new JButton(t); b.setBounds(x,y,w,h);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial",Font.BOLD,13)); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true); b.addActionListener(al); return b;
    }

    class Arena extends JPanel {// inheritance encapuslation
        Arena(){
            setPreferredSize(new Dimension(W,H));
            setLayout(null);
            setFocusable(true);
            addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent e){//polymorphsim
                    keys.add(e.getKeyCode());
                    if(state!=PLAYING) return;
                    int k=e.getKeyCode();
                    if(k==KeyEvent.VK_F)      shoot(1);
                    if(k==KeyEvent.VK_G)      throwBomb(1);
                    if(k==KeyEvent.VK_S)      activateShield(1);
                    if(k==KeyEvent.VK_SLASH)  shoot(2);
                    if(k==KeyEvent.VK_PERIOD) throwBomb(2);
                    if(k==KeyEvent.VK_DOWN)   activateShield(2);
                }
                public void keyReleased(KeyEvent e)//polymorphism
                { keys.remove(e.getKeyCode()); }
            });
            showMenu();
        }

        void showMenu(){
            state=MENU; removeAll();
            JPanel p=darkPanel(200,160,400,165);
            JLabel ttl=new JLabel("Do you wanna fight? ⚔",SwingConstants.CENTER);
            ttl.setForeground(Color.WHITE); ttl.setFont(new Font("Arial",Font.BOLD,18));
            ttl.setBounds(0,14,400,28); p.add(ttl);
            JLabel sub=new JLabel("Best of "+MAX_ROUNDS+" — First to "+WINS_NEEDED+" wins!",SwingConstants.CENTER);
            sub.setForeground(new Color(160,180,220)); sub.setFont(new Font("Arial",Font.ITALIC,13));
            sub.setBounds(0,46,400,22); p.add(sub);
            p.add(btn("⚔  YES, FIGHT!",55,95,135,40,new Color(180,50,50),e->{removeAll();showNameInput();}));
            p.add(btn("✖  No",230,95,90,40,new Color(50,50,100),e->System.exit(0)));
            add(p); revalidate(); repaint(); requestFocusInWindow();
        }

        void showNameInput(){
            state=NAMING; removeAll();
            JPanel p=darkPanel(180,120,440,230);
            JLabel ttl=new JLabel("Enter Player Names",SwingConstants.CENTER);
            ttl.setForeground(Color.WHITE); ttl.setFont(new Font("Arial",Font.BOLD,17));
            ttl.setBounds(0,10,440,28); p.add(ttl);
            p.add(label("P1 (Blue):",20,50,100,P1C));
            f1=field(130,48,150,P1C); p.add(f1);
            p.add(label("P2 (Red):",20,86,100,P2C));
            f2=field(130,84,150,P2C); p.add(f2);
            JLabel c1=new JLabel("<html><b style='color:#5a9fff'>P1:</b> WASD=Move | F=Gun | G=Bomb | S=Shield</html>");
            c1.setBounds(20,124,400,20); p.add(c1);
            JLabel c2=new JLabel("<html><b style='color:#ff5050'>P2:</b> Arrows=Move | /=Gun | .=Bomb | ↓=Shield</html>");
            c2.setBounds(20,150,400,20); p.add(c2);
            p.add(btn("Fight! ⚔",130,178,180,38,new Color(180,50,50),e->startGame()));
            add(p); revalidate(); repaint();
        }

        JLabel label(String t,int x,int y,int w,Color c){
            JLabel l=new JLabel(t); l.setBounds(x,y,w,24);
            l.setForeground(c); l.setFont(new Font("Arial",Font.BOLD,12)); return l;
        }

        JTextField field(int x,int y,int w,Color border){
            JTextField f=new JTextField(); f.setBounds(x,y,w,26);
            f.setBackground(new Color(25,35,65)); f.setForeground(Color.WHITE);
            f.setCaretColor(Color.WHITE); f.setBorder(BorderFactory.createLineBorder(border,1));
            f.setFont(new Font("Arial",Font.PLAIN,13)); return f;
        }

        protected void paintComponent(Graphics g){//polymorphism
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g2.setPaint(new GradientPaint(0,0,SKY1,0,H,SKY2));
            g2.fillRect(0,0,W,H);
            g2.setColor(new Color(255,255,255,160));
            for(int i=0;i<sx.length;i++) g2.fillOval(sx[i],sy[i],ss[i],ss[i]);
            g2.setPaint(new GradientPaint(0,FLOOR+60,GND,0,H,new Color(10,40,10)));
            g2.fillRect(0,FLOOR+60,W,H);
            g2.setColor(new Color(50,130,50)); g2.setStroke(new BasicStroke(2));
            g2.drawLine(0,FLOOR+60,W,FLOOR+60);

            if(state==MENU){drawMenuBg(g2);return;}
            if(state==NAMING) return;

            // Draw game elements
            drawPlayer(g2,p1x,p1y,P1C,P1D,p1hit,p1facingRight,p1walk,p1shield);
            drawPlayer(g2,p2x,p2y,P2C,P2D,p2hit,p2facingRight,p2walk,p2shield);
            if(bActive) drawBullet(g2,bx,by,bDir>0);
            if(bombActive) drawBomb(g2,bombX,bombY);
            if(exploding) drawExplosion(g2,explodeX,explodeY,explodeTimer);
            drawHUD(g2);

            // Winner overlay — drawn on top of everything, visible in ROUND_END state
            if(showWinnerOverlay && state==ROUND_END){
                // Dark tint
                g2.setColor(new Color(0,0,0,120));
                g2.fillRect(0,0,W,H);

                // Colored glow bar
                g2.setColor(new Color(overlayColor.getRed(),overlayColor.getGreen(),overlayColor.getBlue(),60));
                g2.fillRect(0, H/2-70, W, 130);

                // "ROUND OVER" small text
                g2.setFont(new Font("Arial",Font.BOLD,16));
                g2.setColor(new Color(220,220,220,200));
                FontMetrics fm=g2.getFontMetrics();
                String sub="— ROUND "+(currentRound)+" OVER —";
                g2.drawString(sub,(W-fm.stringWidth(sub))/2, H/2-30);

                // Big winner name
                g2.setFont(new Font("Arial",Font.BOLD,52));
                g2.setColor(overlayColor);
                fm=g2.getFontMetrics();
                String winName=roundWinner;
                // Shadow
                g2.setColor(new Color(0,0,0,180));
                g2.drawString(winName,(W-fm.stringWidth(winName))/2+3, H/2+22);
                // Main text
                g2.setColor(overlayColor);
                g2.drawString(winName,(W-fm.stringWidth(winName))/2, H/2+20);

                // "wins!" below
                g2.setFont(new Font("Arial",Font.BOLD,22));
                g2.setColor(Color.WHITE);
                fm=g2.getFontMetrics();
                String winsText="wins the round!";
                g2.drawString(winsText,(W-fm.stringWidth(winsText))/2, H/2+52);
            }
        }

        void drawMenuBg(Graphics2D g2){
            g2.setFont(new Font("Arial",Font.BOLD,50));
            g2.setPaint(new GradientPaint(0,80,P1C,0,135,P2C));
            drawCentered(g2,"⚔ BATTLE ARENA ⚔",115);
            g2.setColor(new Color(200,200,200,170));
            g2.setFont(new Font("Arial",Font.PLAIN,14));
            drawCentered(g2,"Best of "+MAX_ROUNDS+" — First to "+WINS_NEEDED+" rounds wins!",155);
        }

        void drawCentered(Graphics2D g2,String s,int y){
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(s,(W-fm.stringWidth(s))/2,y);
        }

        void drawPlayer(Graphics2D g2,int px,int py,Color c,Color d,boolean hit,boolean right,int walk,boolean shield){
            if(hit){c=new Color(255,255,100);d=new Color(180,160,0);}
            int cx=px+30, groundY=py+60;
            double legSwing=Math.sin(walk*0.25)*12;
            int legOff=(int)legSwing;

            if(shield){
                int pulse=(int)(Math.sin(System.currentTimeMillis()*0.015)*8);
                g2.setColor(new Color(100,200,255,55+pulse));
                g2.fillOval(cx-36,groundY-72,72,80);
                g2.setColor(new Color(150,230,255,160));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx-36,groundY-72,72,80);
            }

            g2.setColor(new Color(0,0,0,50)); g2.fillOval(cx-22,groundY+2,44,8);
            g2.setColor(d); g2.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(cx,groundY-12,cx-10+legOff,groundY);
            g2.drawLine(cx,groundY-12,cx+10-legOff,groundY);
            g2.setColor(new Color(35,35,35)); g2.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(cx-10+legOff,groundY,cx-16+legOff,groundY+2);
            g2.drawLine(cx+10-legOff,groundY,cx+16-legOff,groundY+2);

            int torsoTop=groundY-55;
            g2.setPaint(new GradientPaint(cx-16,torsoTop,c,cx+16,groundY-12,d));
            g2.fillRoundRect(cx-16,torsoTop,32,44,8,8);
            g2.setColor(d); g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cx-16,torsoTop,32,44,8,8);
            g2.setColor(new Color(30,30,30)); g2.fillRect(cx-16,groundY-22,32,5);

            int armSwing=(int)(legSwing*0.4);
            g2.setColor(c); g2.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if(right){
                g2.drawLine(cx-16,torsoTop+8,cx-26,torsoTop+30+armSwing);
                g2.drawLine(cx+16,torsoTop+8,cx+34,torsoTop+22);
            } else {
                g2.drawLine(cx+16,torsoTop+8,cx+26,torsoTop+30-armSwing);
                g2.drawLine(cx-16,torsoTop+8,cx-34,torsoTop+22);
            }
            g2.setColor(c); g2.setStroke(new BasicStroke(7,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(cx,torsoTop-3,cx,torsoTop);

            int hy=torsoTop-28;
            g2.setPaint(new GradientPaint(cx-14,hy,c,cx+14,hy+28,d));
            g2.fillOval(cx-14,hy,28,28);
            g2.setColor(d); g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx-14,hy,28,28);
            g2.setColor(Color.WHITE);
            int eyeX=right?cx+3:cx-10;
            g2.fillOval(eyeX,hy+8,6,6);
            g2.setColor(new Color(15,15,50));
            g2.fillOval(eyeX+(right?1:-1),hy+10,3,3);
            g2.setColor(d); g2.setStroke(new BasicStroke(3));
            for(int i=-10;i<=10;i+=5) g2.drawLine(cx+i,hy,cx+i,hy-7);
            drawGun(g2,cx,torsoTop+22,right);
        }

        void drawGun(Graphics2D g2,int cx,int gy,boolean right){
            int gx=right?cx+18:cx-46;
            g2.setColor(GUN); g2.fillRoundRect(gx,gy-4,28,9,3,3);
            g2.setColor(new Color(45,45,45));
            if(right) g2.fillRect(gx+26,gy-2,12,5); else g2.fillRect(gx-12,gy-2,12,5);
            g2.setColor(new Color(90,55,15)); g2.fillRoundRect(gx+5,gy+4,10,12,3,3);
            g2.setColor(new Color(130,130,130)); g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(gx+2,gy-1,gx+24,gy-1);
        }

        void drawBullet(Graphics2D g2,int bx,int by,boolean right){
            g2.setColor(new Color(255,200,0,70)); g2.fillOval(bx-8,by-8,16,16);
            g2.setColor(BULL); g2.fillOval(bx-4,by-4,9,9);
            g2.setColor(new Color(255,140,0,110));
            if(right) g2.fillRect(bx-18,by-2,14,4); else g2.fillRect(bx+4,by-2,14,4);
        }

        void drawBomb(Graphics2D g2,int bx,int by){
            g2.setColor(new Color(30,30,30)); g2.fillOval(bx-10,by-10,20,20);
            g2.setColor(new Color(80,80,80)); g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(bx-10,by-10,20,20);
            if((bombTimer/4)%2==0){g2.setColor(new Color(255,200,50));g2.fillOval(bx-2,by-18,5,5);}
            g2.setColor(new Color(120,80,40)); g2.setStroke(new BasicStroke(2));
            g2.drawLine(bx,by-10,bx+2,by-16);
        }

        void drawExplosion(Graphics2D g2,int ex,int ey,int t){
            int r1=(int)(t*3.5), r2=(int)(t*2.2);
            g2.setColor(new Color(255,120,0,Math.max(0,200-t*5))); g2.fillOval(ex-r1,ey-r1,r1*2,r1*2);
            g2.setColor(new Color(255,220,0,Math.max(0,220-t*5))); g2.fillOval(ex-r2,ey-r2,r2*2,r2*2);
            if(t<15){g2.setColor(new Color(255,255,220,200));g2.fillOval(ex-15,ey-15,30,30);}
        }

        void drawHUD(Graphics2D g2){
            g2.setFont(new Font("Arial",Font.BOLD,14));
            g2.setColor(new Color(255,220,80));
            drawCentered(g2,"Round "+currentRound+" / "+MAX_ROUNDS,15);

            drawWinDots(g2,30,22,p1Wins,P1C);
            drawWinDots(g2,W-30-(WINS_NEEDED*18),22,p2Wins,P2C);

            drawBar(g2,20,26,p1Name,p1hp,P1C,true,p1shield,p1shieldCooldown,p1shieldTimer);
            drawBar(g2,W-260,26,p2Name,p2hp,P2C,false,p2shield,p2shieldCooldown,p2shieldTimer);

            g2.setFont(new Font("Arial",Font.BOLD,18));
            g2.setColor(new Color(255,210,50));
            drawCentered(g2,"VS",46);

            g2.setFont(new Font("Arial",Font.PLAIN,10));
            g2.setColor(new Color(200,200,200,130));
            g2.drawString("WASD|F=Gun G=Bomb S=Shield",6,H-6);
            g2.drawString("Arrows|/=Gun .=Bomb ↓=Shield",W-170,H-6);

            if(p1BombCooldown>0){g2.setColor(new Color(255,150,0,180));g2.drawString("Bomb:"+(p1BombCooldown/60+1)+"s",6,H-18);}
            if(p2BombCooldown>0){g2.setColor(new Color(255,150,0,180));g2.drawString("Bomb:"+(p2BombCooldown/60+1)+"s",W-75,H-18);}
        }

        void drawWinDots(Graphics2D g2,int x,int y,int wins,Color c){
            for(int i=0;i<WINS_NEEDED;i++){
                if(i<wins) g2.setColor(c); else g2.setColor(new Color(60,60,80));
                g2.fillOval(x+i*18,y,12,12);
                g2.setColor(new Color(0,0,0,80)); g2.setStroke(new BasicStroke(1));
                g2.drawOval(x+i*18,y,12,12);
            }
        }

        void drawBar(Graphics2D g2,int x,int y,String name,int hp,Color c,boolean left,
                     boolean shield,int shieldCooldown,int shieldTimer){
            int bw=230,bh=20;
            g2.setFont(new Font("Arial",Font.BOLD,13)); g2.setColor(c);
            if(left) g2.drawString(name,x,y+12);
            else{FontMetrics fm=g2.getFontMetrics();g2.drawString(name,x+bw-fm.stringWidth(name),y+12);}
            g2.setColor(new Color(40,40,40)); g2.fillRoundRect(x,y+14,bw,bh,7,7);
            int fw=(int)(hp/100.0*bw);
            Color hc=hp>40?c:new Color(220,50,50);
            g2.setPaint(new GradientPaint(x,y+14,hc.brighter(),x,y+14+bh,hc.darker()));
            if(fw>0) g2.fillRoundRect(x,y+14,fw,bh,7,7);
            g2.setColor(new Color(180,180,180,70)); g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x,y+14,bw,bh,7,7);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,11));
            String hs=hp+" HP"; FontMetrics fm=g2.getFontMetrics();
            g2.drawString(hs,x+(bw-fm.stringWidth(hs))/2,y+27);

            if(shield){
                int sw=(int)(shieldTimer/(double)SHIELD_DURATION*bw);
                g2.setColor(new Color(0,200,255,160));
                g2.fillRoundRect(x,y+36,sw,6,3,3);
                g2.setColor(new Color(100,230,255,200)); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(x,y+36,bw,6,3,3);
            } else if(shieldCooldown>0){
                int sw=(int)((SHIELD_COOLDOWN-shieldCooldown)/(double)SHIELD_COOLDOWN*bw);
                g2.setColor(new Color(80,80,120,160));
                g2.fillRoundRect(x,y+36,bw,6,3,3);
                g2.setColor(new Color(160,160,200,160));
                g2.fillRoundRect(x,y+36,sw,6,3,3);
            }
        }
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(Game::new); }
}