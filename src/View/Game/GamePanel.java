package View.Game;

import Model.Lane.Lane;
import Model.Level.LevelData;
import Model.Pea.FreezePea;
import Model.Pea.Pea;
import Model.Plant.FreezePeashooter;
import Model.Plant.Peashooter;
import Model.Plant.Sunflower;
import Model.Zombie.Zombie;
import View.Collider;
import View.Element.Sun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Armin on 6/25/2016.
 */
public class GamePanel extends JLayeredPane implements MouseMotionListener {

    private Image backgroundImage = new ImageIcon(this.getClass().getResource("../../images/mainBG.png")).getImage();

    private Collider[] colliders;

    private ArrayList<ArrayList<Zombie>> laneZombies;
    private ArrayList<ArrayList<Pea>> lanePeas;
    private ArrayList<Sun> activeSuns;

    private Timer redrawTimer;
    private Timer advancerTimer;
    private Timer sunProducer;
    private Timer zombieProducer;
    private JLabel sunScoreboard;

    public static final int WINDOW_WIDTH = 1012;
    public static final int WINDOW_HEIGHT = 785;
    public static final int INIT_SCORE = 150;
    public static final int NUMBER_OF_ROW_BLOCK = 9;
    public static final int NUMBER_OF_COLUMN_BLOCK = 9;
    public static final int NUMBER_OF_BLOCK = NUMBER_OF_ROW_BLOCK * NUMBER_OF_COLUMN_BLOCK;
    public static final int REDRAW_DELAY = 25;
    public static final int ADVANCER_DELAY = 60;
    public static final int PRODUCE_SUN_DELAY = 5000;
    public static final int PRODUCE_ZOMBIE_DELAY = 7000;
    public static final int SUNFLOWER_COST = 50;
    public static final int PEASHOOTER_COST = 100;
    public static final int FREEZEPEASHOOTER_COST = 175;

    private GameFrame.PlantType activePlantingBrush = GameFrame.PlantType.None;

    private int mouseX, mouseY;
    private int sunScore;
    
    public GamePanel(JLabel sunScoreboard) {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout(null);
        addMouseMotionListener(this);
        this.sunScoreboard = sunScoreboard;
        setSunScore(INIT_SCORE);

        laneZombies = makeLaneZombies();
        lanePeas = makeLanePea();
        colliders = makeColliders();
        activeSuns = new ArrayList<>();

        setTimer();
    }

    public int getSunScore() {
        return sunScore;
    }

    public void setSunScore(int sunScore) {
        this.sunScore = sunScore;
        sunScoreboard.setText(String.valueOf(sunScore));
    }

    public ArrayList<ArrayList<Zombie>> makeLaneZombies() {
        ArrayList<ArrayList<Zombie>> laneZombies = new ArrayList<>();
        laneZombies.add(new ArrayList<>()); //line 1
        laneZombies.add(new ArrayList<>()); //line 2
        laneZombies.add(new ArrayList<>()); //line 3
        laneZombies.add(new ArrayList<>()); //line 4
        laneZombies.add(new ArrayList<>()); //line 5
        return laneZombies;
    }

    public ArrayList<ArrayList<Pea>> makeLanePea() {
        ArrayList<ArrayList<Pea>> lanePea = new ArrayList<>();
        lanePea.add(new ArrayList<>()); //line 1
        lanePea.add(new ArrayList<>()); //line 2
        lanePea.add(new ArrayList<>()); //line 3
        lanePea.add(new ArrayList<>()); //line 4
        lanePea.add(new ArrayList<>()); //line 5
        return lanePea;
    }

    public Collider[] makeColliders() {
        Collider[] colliders = new Collider[NUMBER_OF_BLOCK];
        for (int i = 0; i < NUMBER_OF_BLOCK; i++) {
            Collider collider = new Collider();
            collider.setLocation(44 + (i % NUMBER_OF_ROW_BLOCK) * 100, 109 + (i / NUMBER_OF_ROW_BLOCK) * 120);
            collider.setAction(new PlantActionListener((i % NUMBER_OF_ROW_BLOCK), (i / NUMBER_OF_ROW_BLOCK)));
            colliders[i] = collider;
            add(collider, new Integer(0));
        }
        return colliders;
    }

    public void setTimer() {
        redrawTimer = new Timer(REDRAW_DELAY, handleRedraw());
        advancerTimer = new Timer(ADVANCER_DELAY, handleAdvancer());
        sunProducer = new Timer(PRODUCE_SUN_DELAY, handleProduceSun());
        zombieProducer = new Timer(PRODUCE_ZOMBIE_DELAY, handleProduceZombie());

        redrawTimer.start();
        advancerTimer.start();
        sunProducer.start();
        zombieProducer.start();
    }

    public ActionListener handleRedraw() {
        return (ActionEvent e) -> repaint();
    }
    public ActionListener handleAdvancer() {
        return (ActionEvent e) -> advance();
    }
    public ActionListener handleProduceSun() {
        return (ActionEvent e) -> {
            Random rnd = new Random();
            Sun sta = new Sun(this, rnd.nextInt(800) + 100, 0, rnd.nextInt(300) + 200);
            activeSuns.add(sta);
            add(sta, new Integer(1));
        };
    }
    public ActionListener handleProduceZombie() {
        return (ActionEvent e) -> {
            Random rnd = new Random();
            LevelData levelData = new LevelData();
            String[] Level = levelData.LEVEL_CONTENT[Integer.parseInt(levelData.LEVEL_NUMBER) - 1];
            int[][] LevelValue = levelData.LEVEL_VALUE[Integer.parseInt(levelData.LEVEL_NUMBER) - 1];
            int l = rnd.nextInt(5);
            int t = rnd.nextInt(100);
            Zombie zombie = null;
            for (int i = 0; i < LevelValue.length; i++) {
                if (t < LevelValue[i][0] || t > LevelValue[i][1]) continue;
                zombie = Zombie.getZombie(Level[i], GamePanel.this, l);
            }
            laneZombies.get(l).add(zombie);
        };
    }

    private void advance() {
        for (int i = 0; i < 5; i++) {
            for (Zombie zombie : laneZombies.get(i)) {
                zombie.advance();
            }
            
            peaAdvance(i);
        }
        for (int i = 0; i < activeSuns.size(); i++) {
            activeSuns.get(i).advance();
        }
    }

    private void peaAdvance(int laneIndex) {
        for (int j = 0; j < lanePeas.get(laneIndex).size(); j++) {
            Pea pea = lanePeas.get(laneIndex).get(j);
            Rectangle peaRectangle = new Rectangle(pea.getPositionX(), 130 + pea.getMyLane() * 120, 28, 28);
            for (int zombieIndex = 0; zombieIndex < getLaneZombies().get(pea.getMyLane()).size(); zombieIndex++) {
                Zombie zombie = getLaneZombies().get(pea.getMyLane()).get(zombieIndex);
                Rectangle zombieRectangle = new Rectangle(zombie.getPosX(), 109 + pea.getMyLane() * 120, 400, 120);
                if (peaRectangle.intersects(zombieRectangle)) {
                    zombie.setHealth(zombie.getHealth() - pea.getPower());
                    if (pea instanceof FreezePea)
                        zombie.slow();
                    boolean exit = false;
                    
                    getLaneZombies().get(pea.getMyLane()).remove(pea);
                    if (exit) break;
                }
            }
            pea.advance();
        }
    }
    
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(backgroundImage, 0, 0, null);

        //Draw Plants
        for (int i = 0; i < NUMBER_OF_BLOCK; i++) {
            if (colliders[i].assignedPlant == null) continue;
            colliders[i].assignedPlant
                    .getPlantView()
                    .draw(60 + (i % 9) * 100, 129 + (i / 9) * 120, graphics);
        }
        for (int i = 0; i < 5; i++) {
            for (Zombie zombie : laneZombies.get(i)) {
                zombie.getView().draw(zombie.getPosX(), 109 + (i * 120), graphics);
            }
            for (int j = 0; j < lanePeas.get(i).size(); j++) {
                Pea pea = lanePeas.get(i).get(j);
                pea.getView().draw(pea.getPositionX(), 130 + (i * 120), graphics);
            }
        }
    }

    private class PlantActionListener implements ActionListener {

        int x, y;

        public PlantActionListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean isSunflower = activePlantingBrush == GameFrame.PlantType.Sunflower;
            boolean isPeashooter = activePlantingBrush == GameFrame.PlantType.Peashooter;
            boolean isFreezePeashooter = activePlantingBrush == GameFrame.PlantType.FreezePeashooter;

            boolean isLargeSunflowerCost = getSunScore() >= SUNFLOWER_COST;
            boolean isLargePeashooterCost = getSunScore() >= PEASHOOTER_COST;
            boolean isLargeFreezePeashooterCost = getSunScore() >= FREEZEPEASHOOTER_COST;

            if(isSunflower && isLargeSunflowerCost) {
                colliders[x + y * 9].setPlant(new Sunflower(GamePanel.this, x, y));
                setSunScore(getSunScore() - SUNFLOWER_COST);
                return;
            }
            if(isPeashooter && isLargePeashooterCost) {
                colliders[x + y * 9].setPlant(new Peashooter(GamePanel.this, x, y));
                setSunScore(getSunScore() - PEASHOOTER_COST);
                return;
            }
            if(isFreezePeashooter && isLargeFreezePeashooterCost) {
                colliders[x + y * 9].setPlant(new FreezePeashooter(GamePanel.this, x, y));
                setSunScore(getSunScore() - FREEZEPEASHOOTER_COST);
                return;
            }

            activePlantingBrush = GameFrame.PlantType.None;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    static int progress = 0;

    public static void setProgress(int num) {
        progress = progress + num;
        System.out.println(progress);
        if(progress < 150) return;
        if ("1".equals(LevelData.LEVEL_NUMBER)) {
            JOptionPane.showMessageDialog(null, "LEVEL_CONTENT Completed !!!" + '\n' + "Starting next LEVEL_CONTENT");
            GameFrame.gameFrame.dispose();
            LevelData.write("2");
            GameFrame.gameFrame = new GameFrame();
        } else {
            JOptionPane.showMessageDialog(null, "LEVEL_CONTENT Completed !!!" + '\n' + "More Levels will come soon !!!" + '\n' + "Resetting data");
            LevelData.write("1");
            System.exit(0);
        }
        progress = 0;
    }

    public GameFrame.PlantType getActivePlantingBrush() {
        return activePlantingBrush;
    }

    public void setActivePlantingBrush(GameFrame.PlantType activePlantingBrush) {
        this.activePlantingBrush = activePlantingBrush;
    }

    public ArrayList<ArrayList<Zombie>> getLaneZombies() {
        return laneZombies;
    }

    public void setLaneZombies(ArrayList<ArrayList<Zombie>> laneZombies) {
        this.laneZombies = laneZombies;
    }

    public ArrayList<ArrayList<Pea>> getLanePeas() {
        return lanePeas;
    }

    public void setLanePeas(ArrayList<ArrayList<Pea>> lanePeas) {
        this.lanePeas = lanePeas;
    }

    public ArrayList<Sun> getActiveSuns() {
        return activeSuns;
    }

    public void setActiveSuns(ArrayList<Sun> activeSuns) {
        this.activeSuns = activeSuns;
    }

    public Collider[] getColliders() {
        return colliders;
    }

    public void setColliders(Collider[] colliders) {
        this.colliders = colliders;
    }
}
