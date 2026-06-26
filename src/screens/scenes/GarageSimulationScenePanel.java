package screens.scenes;

import Controllers.GarageSimulatorController;
import Models.HomePageModel;
import ilcompiler.input.Input.InputType;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GarageSimulationScenePanel extends JPanel implements IScenePanel {

    private InputEventListener inputListener;
    private Runnable onCriticalFailureCallback;

    private final GarageSimulatorController controller;

    private final PushButton btnOpen, btnClose, btnStop;
    private final RedIndicator ledOpen, ledAjar, ledClosed;
    private final RedIndicator sensorTop, sensorBottom;
    private final RedIndicator ledMotorUp, ledMotorDown;

    private Image imgBackground;
    private Image imgDoor;
    private Image imgMotorOn;
    private Image imgMotorOff;

    // --- NOVA VARIÁVEL DE CONTROLE DE ERRO ---
    private boolean errorShown = false;

    public GarageSimulationScenePanel() {
        this.setLayout(null);
        this.setBackground(new Color(50, 50, 50));
        this.setPreferredSize(new java.awt.Dimension(624, 394));
        this.setSize(624, 394);

        loadAssets();

        controller = new GarageSimulatorController(this);

        btnOpen = new PushButton("I0.0", InputType.NO);
        btnClose = new PushButton("I0.1", InputType.NO);
        btnStop = new PushButton("I0.2", InputType.NC, PushButton.ButtonPalette.RED);

        ledOpen = new RedIndicator("Q1.0", RedIndicator.IndicatorType.LED);
        ledAjar = new RedIndicator("Q1.1", RedIndicator.IndicatorType.LED);
        ledClosed = new RedIndicator("Q1.2", RedIndicator.IndicatorType.LED);

        // CONFIGURAÇÃO DO LED DO MOTOR (Verde)
        ledMotorUp = new RedIndicator("Q0.0", RedIndicator.IndicatorType.LED); // Mantido tipo LED, mas será desenhado quadrado
        ledMotorDown = new RedIndicator("Q0.1", RedIndicator.IndicatorType.LED);

        sensorTop = new RedIndicator("I1.0", RedIndicator.IndicatorType.SENSOR);
        sensorBottom = new RedIndicator("I1.1", RedIndicator.IndicatorType.SENSOR);

        initComponents();
    }

    private void loadAssets() {
        try {
            imgBackground = new ImageIcon(getClass().getResource("/Assets/GarageSimulation/Background_f1.png"))
                    .getImage();
            imgDoor = new ImageIcon(getClass().getResource("/Assets/GarageSimulation/Portao.png")).getImage();
        } catch (Exception e) {
            System.err.println("ERRO: Imagens não encontradas na pasta Assets.");
        }
    }

    private void initComponents() {
        // Botões
        addLabel("ABRIR (I0.0)", 20, 100);
        btnOpen.setBounds(20, 120, 100, 40);
        this.add(btnOpen);

        addLabel("FECHAR (I0.1)", 20, 170);
        btnClose.setBounds(20, 190, 100, 40);
        this.add(btnClose);

        addLabel("PARAR (I0.2)", 20, 240);
        btnStop.setBounds(20, 260, 100, 40);
        this.add(btnStop);

        // LEDs Status
        int xStatus = 500;
        addLabel("Aberto (Q1.0)", xStatus, 140);
        ledOpen.setBounds(xStatus + 90, 140, 20, 20);
        this.add(ledOpen);

        addLabel("Meio (Q1.1)", xStatus, 180);
        ledAjar.setBounds(xStatus + 90, 180, 20, 20);
        this.add(ledAjar);

        addLabel("Fechado (Q1.2)", xStatus, 220);
        ledClosed.setBounds(xStatus + 90, 220, 20, 20);
        this.add(ledClosed);

        // Sensores
        addLabel("Sensor Alto: I1.0", 508, 78);
        sensorTop.setBounds(495, 83, 15, 15);
        this.add(sensorTop);

        addLabel("Sensor Baixo: I1.1", 25, 330);
        sensorBottom.setBounds(130, 335, 10, 10);
        this.add(sensorBottom);
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(x, y, 120, 20);
        this.add(lbl);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Fundo
        if (imgBackground != null) {
            g2d.drawImage(imgBackground, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Portão
        int doorStartX = 0;
        int doorWidth = getWidth();
        int doorTopLimitY = 82;
        int doorBottomLimitY = 460;

        int currentPhysicalY = controller.getDoorY();
        int visualBottomY = mapRange(currentPhysicalY, 0, 295, doorTopLimitY, doorBottomLimitY);

        Shape oldClip = g2d.getClip();
        g2d.setClip(doorStartX, doorTopLimitY, doorWidth, getHeight() - doorTopLimitY);

        if (imgDoor != null) {
            int imgH = imgDoor.getHeight(this);
            g2d.drawImage(imgDoor, doorStartX, visualBottomY - imgH, doorWidth, imgH, this);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(doorStartX, doorTopLimitY, doorWidth, visualBottomY - doorTopLimitY);
        }
        g2d.setClip(oldClip);

        // Motor
        boolean motorRunning = false;
        try {
            Map<String, Boolean> outs = HomePageModel.getOutputs();
            if (outs != null) {
                boolean up = outs.getOrDefault("Q0.0", false);
                boolean down = outs.getOrDefault("Q0.1", false);
                motorRunning = (up || down) && HomePageModel.isRunning();
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler saídas do motor: " + e.getMessage());
        }

        Image motorToDraw = motorRunning ? imgMotorOn : imgMotorOff;

        if (motorToDraw != null) {
            g2d.drawImage(motorToDraw, 105, 80, 80, 60, this);
        } 
        
        // --- LED DO MOTOR (Recuperado) ---
        // Desenha o quadrado colorido manualmente para indicar status visualmente
        // Verde se ligado, Vermelho se desligado/parado
        g2d.setColor(motorRunning ? Color.GREEN : Color.RED);
        g2d.fillOval(122, 75, 5, 5); // Coordenadas originais do LED
    }

    private int mapRange(int val, int inMin, int inMax, int outMin, int outMax) {
        return (val - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    @Override
    public void initInputs(Map<String, InputType> inputsType, Map<String, Boolean> inputs) {
        inputsType.put("I0.0", InputType.NO);
        inputsType.put("I0.1", InputType.NO);
        inputsType.put("I0.2", InputType.NC);
        inputsType.put("I1.0", InputType.NO);
        inputsType.put("I1.1", InputType.NO);

        inputs.put("I0.0", false);
        inputs.put("I0.1", false);
        inputs.put("I0.2", true);
        inputs.put("I1.0", false);
        inputs.put("I1.1", true);
    }

    @Override
    public void updateUIState(Map<String, InputType> inputsType, Map<String, Boolean> inputs,
            Map<String, Boolean> outputs) {
        ledOpen.setActive(outputs.getOrDefault("Q1.0", false));
        ledAjar.setActive(outputs.getOrDefault("Q1.1", false));
        ledClosed.setActive(outputs.getOrDefault("Q1.2", false));

        boolean motorUp = outputs.getOrDefault("Q0.0", false);
        boolean motorDown = outputs.getOrDefault("Q0.1", false);
        ledMotorUp.setActive(motorUp || motorDown);

        if (HomePageModel.isRunning()) {
            if (motorUp)
                controller.startOpening();
            else
                controller.stopOpening();
            if (motorDown)
                controller.startClosing();
            else
                controller.stopClosing();
        }

        inputs.put("I1.0", controller.getDoorY() <= 60);
        inputs.put("I1.1", controller.getDoorY() >= 330);
        sensorTop.setActive(inputs.get("I1.0"));
        sensorBottom.setActive(inputs.get("I1.1"));

        // --- CORREÇÃO DO LOOP DE POP-UPS ---
        if (controller.motorDamaged && !errorShown && onCriticalFailureCallback != null) {
            errorShown = true; // Trava para não abrir mais janelas

            // Pausa a simulação
            onCriticalFailureCallback.run();

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "MOTOR QUEIMADO! Falha crítica.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                resetUIState();
            });
        }
        this.repaint();
    }

    @Override
    public void resetUIState() {
        controller.reset();
        errorShown = false; // Destrava para permitir nova simulação
        repaint();
    }

    @Override
    public void stop() {
        controller.stopOpening();
        controller.stopClosing();
    }

    @Override
    public void setInputListener(InputEventListener listener) {
        this.inputListener = listener;

        InputEventListener protectedListener = new InputEventListener() {
            @Override
            public void onPressed(String inputKey, java.awt.event.MouseEvent evt) {
                if (HomePageModel.isRunning() && controller.isMotorRunning()) {
                    if (inputKey.equals("I0.0") || inputKey.equals("I0.1")) {
                        controller.triggerDamage();
                        // NÃO chama JOptionPane aqui. Deixa o updateUIState lidar com isso de forma
                        // segura.
                    }
                } else if (listener != null) {
                    listener.onPressed(inputKey, evt);
                }
            }

            @Override
            public void onReleased(String inputKey, java.awt.event.MouseEvent evt) {
                if (listener != null)
                    listener.onReleased(inputKey, evt);
            }
        };

        btnOpen.setInputEventListener(protectedListener);
        btnClose.setInputEventListener(protectedListener);
        btnStop.setInputEventListener(listener);
    }

    @Override
    public void setOnCriticalFailureCallback(Runnable callback) {
        this.onCriticalFailureCallback = callback;
    }
}