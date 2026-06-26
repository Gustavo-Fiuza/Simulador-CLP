package screens.scenes;

import Controllers.ElevatorSimulatorController;
import Models.HomePageModel;
import ilcompiler.input.Input.InputType;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ElevatorSimulationScenePanel extends JPanel implements IScenePanel {

    private InputEventListener inputListener;
    private Runnable onCriticalFailureCallback;

    private final ElevatorSimulatorController controller;

    private final PushButton btnCallA1, btnCallA2, btnEmergency;
    private final RedIndicator ledA1, ledTransit, ledA2;
    private final RedIndicator sensorA1, sensorA2, sensorDoor;
    private final RedIndicator ledMotorUp, ledMotorDown, ledDoorActuator;

    private Image imgBackground;
    private Image imgDoorLeft;
    private Image imgDoorRight;
    // private Image imgCabin;

    private boolean errorShown = false;

    public ElevatorSimulationScenePanel() {
        this.setLayout(null);
        this.setBackground(new Color(50, 50, 50));
        this.setPreferredSize(new java.awt.Dimension(624, 394));
        this.setSize(624, 394);

        loadAssets();

        controller = new ElevatorSimulatorController(this);

        // Mapeamento idêntico à garagem
        btnCallA1 = new PushButton("I0.0", InputType.NO);
        btnCallA2 = new PushButton("I0.1", InputType.NO);
        btnEmergency = new PushButton("I0.6", InputType.NC, PushButton.ButtonPalette.RED);

        ledA1 = new RedIndicator("Q1.0", RedIndicator.IndicatorType.LED);
        ledTransit = new RedIndicator("Q1.1", RedIndicator.IndicatorType.LED);
        ledA2 = new RedIndicator("Q1.2", RedIndicator.IndicatorType.LED);

        ledMotorUp = new RedIndicator("Q0.0", RedIndicator.IndicatorType.LED);
        ledMotorDown = new RedIndicator("Q0.1", RedIndicator.IndicatorType.LED);
        ledDoorActuator = new RedIndicator("Q0.2", RedIndicator.IndicatorType.LED);

        sensorA1 = new RedIndicator("I0.2", RedIndicator.IndicatorType.SENSOR);
        sensorA2 = new RedIndicator("I0.3", RedIndicator.IndicatorType.SENSOR);
        sensorDoor = new RedIndicator("I0.5", RedIndicator.IndicatorType.SENSOR);

        initComponents();
    }

    private void loadAssets() {
        try {
            imgBackground = new ImageIcon(getClass().getResource("/Assets/ElevatorSimulation/elevador-dois-andares.png")).getImage();
            imgDoorLeft = new ImageIcon(getClass().getResource("/Assets/ElevatorSimulation/porta-esquerda.png")).getImage();
            imgDoorRight = new ImageIcon(getClass().getResource("/Assets/ElevatorSimulation/porta-direita.png")).getImage();
            // imgCabin = new ImageIcon(getClass().getResource("/Assets/ElevatorSimulation/Cabin.png")).getImage();
        } catch (Exception e) {
            System.err.println("ERRO: Imagens não encontradas.");
        }
    }

    private void initComponents() {
        // Botões (Esquerda)
        addLabel("CHAMAR A1 (I0.0)", 20, 80);
        btnCallA1.setBounds(20, 100, 100, 35);
        this.add(btnCallA1);

        addLabel("CHAMAR A2 (I0.1)", 20, 150);
        btnCallA2.setBounds(20, 170, 100, 35);
        this.add(btnCallA2);

        addLabel("EMERGÊNCIA (I0.6)", 20, 220);
        btnEmergency.setBounds(20, 240, 100, 35);
        this.add(btnEmergency);

        // LEDs e Sensores (Direita)
        int xStatus = 480;
        
        addLabel("Sensor A1 (I0.2)", xStatus, 40);
        sensorA1.setBounds(xStatus + 110, 42, 16, 16);
        this.add(sensorA1);

        addLabel("Sensor A2 (I0.3)", xStatus, 70);
        sensorA2.setBounds(xStatus + 110, 72, 16, 16);
        this.add(sensorA2);

        addLabel("Porta Fec (I0.5)", xStatus, 100);
        sensorDoor.setBounds(xStatus + 110, 102, 16, 16);
        this.add(sensorDoor);

        addLabel("Atuador Porta (Q0.2)", xStatus, 150);
        ledDoorActuator.setBounds(xStatus + 110, 152, 16, 16);
        this.add(ledDoorActuator);

        addLabel("Motor Sobe (Q0.0)", xStatus, 180);
        ledMotorUp.setBounds(xStatus + 110, 182, 16, 16);
        this.add(ledMotorUp);

        addLabel("Motor Desce (Q0.1)", xStatus, 210);
        ledMotorDown.setBounds(xStatus + 110, 212, 16, 16);
        this.add(ledMotorDown);

        addLabel("LED A1 (Q1.0)", xStatus, 260);
        ledA1.setBounds(xStatus + 110, 262, 16, 16);
        this.add(ledA1);

        addLabel("LED Trânsito (Q1.1)", xStatus, 290);
        ledTransit.setBounds(xStatus + 110, 292, 16, 16);
        this.add(ledTransit);

        addLabel("LED A2 (Q1.2)", xStatus, 320);
        ledA2.setBounds(xStatus + 110, 322, 16, 16);
        this.add(ledA2);
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
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

        int off1 = controller.getDoorOffsetA1();
        int off2 = controller.getDoorOffsetA2();
        
        // --- 🛠️ VARIÁVEIS DE POSIÇÃO E TAMANHO (CRIADAS AQUI) 🛠️ ---
        int xCenter = 312;  // Centro do portal
        int wDoor = 40;     // Largura da porta
        int hDoor = 115;    // Altura da porta
        
        int yAndar2 = 50;   // Altura Y do andar de cima
        int yAndar1 = 251;  // Altura Y do andar de baixo

        // // --- 🟩 MODO RAIO-X PARA ALINHAMENTO 🟩 ---
        // g2d.setColor(Color.GREEN);
        // g2d.drawRect((xCenter - wDoor) - off1, yAndar1, wDoor, hDoor);
        // g2d.drawRect(xCenter + off1, yAndar1, wDoor, hDoor);

        // g2d.setColor(Color.YELLOW);
        // g2d.drawRect((xCenter - wDoor) - off1, yAndar2, wDoor, hDoor);
        // g2d.drawRect(xCenter + off1, yAndar2, wDoor, hDoor);
        
        // g2d.setColor(Color.RED);
        // g2d.drawLine(xCenter, 0, xCenter, getHeight());
        // // ------------------------------------------

        // // Portas A2 (Nível Superior)
        // if (imgDoorLeft != null && imgDoorRight != null) {
        //     g2d.drawImage(imgDoorLeft, (xCenter - wDoor) - off2, yAndar2, wDoor, hDoor, this);
        //     g2d.drawImage(imgDoorRight, xCenter + off2, yAndar2, wDoor, hDoor, this);
        // }
        
        // // Portas A1 (Nível Inferior)
        // if (imgDoorLeft != null && imgDoorRight != null) {
        //     g2d.drawImage(imgDoorLeft, (xCenter - wDoor) - off1, yAndar1, wDoor, hDoor, this);
        //     g2d.drawImage(imgDoorRight, xCenter + off1, yAndar1, wDoor, hDoor, this);
        // }

        // --- 🪄 ÁREA DE CORTE (MÁSCARA) 🪄 ---
        // Calcula onde começa o vão do elevador e qual a largura total dele
        int portalX = xCenter - wDoor;
        int portalWidth = wDoor * 2; 

        // Salva a área de desenho original da tela para não quebrar os outros componentes
        java.awt.Shape oldClip = g2d.getClip();

        // 1. Renderiza o Andar 2 (Apenas dentro da máscara)
        g2d.setClip(portalX, yAndar2, portalWidth, hDoor);
        if (imgDoorLeft != null && imgDoorRight != null) {
            g2d.drawImage(imgDoorLeft, (xCenter - wDoor) - off2, yAndar2, wDoor, hDoor, this);
            g2d.drawImage(imgDoorRight, xCenter + off2, yAndar2, wDoor, hDoor, this);
        }
        
        // 2. Renderiza o Andar 1 (Apenas dentro da máscara)
        g2d.setClip(portalX, yAndar1, portalWidth, hDoor);
        if (imgDoorLeft != null && imgDoorRight != null) {
            g2d.drawImage(imgDoorLeft, (xCenter - wDoor) - off1, yAndar1, wDoor, hDoor, this);
            g2d.drawImage(imgDoorRight, xCenter + off1, yAndar1, wDoor, hDoor, this);
        }

        // Restaura a tela original para que os LEDs e painéis não sumam
        g2d.setClip(oldClip);
    }

    @Override
    public void initInputs(Map<String, InputType> inputsType, Map<String, Boolean> inputs) {
        inputsType.put("I0.0", InputType.NO);
        inputsType.put("I0.1", InputType.NO);
        inputsType.put("I0.2", InputType.NO);
        inputsType.put("I0.3", InputType.NO);
        inputsType.put("I0.5", InputType.NO);
        inputsType.put("I0.6", InputType.NC);

        inputs.put("I0.0", false);
        inputs.put("I0.1", false);
        inputs.put("I0.2", true);
        inputs.put("I0.3", false);
        inputs.put("I0.5", false);
        inputs.put("I0.6", true);
    }

    @Override
    public void updateUIState(Map<String, InputType> inputsType, Map<String, Boolean> inputs,
            Map<String, Boolean> outputs) {
            
        if (outputs == null || inputs == null) return;

        // Segurança para evitar o NullPointer do HomePg.java
        if (!inputsType.containsKey("I0.0")) {
            initInputs(inputsType, inputs);
        }

        ledA1.setActive(outputs.getOrDefault("Q1.0", false));
        ledTransit.setActive(outputs.getOrDefault("Q1.1", false));
        ledA2.setActive(outputs.getOrDefault("Q1.2", false));

        boolean motorUp = outputs.getOrDefault("Q0.0", false);
        boolean motorDown = outputs.getOrDefault("Q0.1", false);
        boolean doorActuator = outputs.getOrDefault("Q0.2", false);
        
        ledMotorUp.setActive(motorUp);
        ledMotorDown.setActive(motorDown);
        ledDoorActuator.setActive(doorActuator);

        if (HomePageModel.isRunning()) {
            if (motorUp) controller.startCabinUp(); else controller.stopCabinUp();
            if (motorDown) controller.startCabinDown(); else controller.stopCabinDown();
            if (doorActuator) controller.startClosingDoors(); else controller.startOpeningDoors();
        }

        inputs.put("I0.2", controller.getCabinY() >= 251);
        inputs.put("I0.3", controller.getCabinY() <= 50);
        inputs.put("I0.5", controller.isDoorFullyClosed());

        sensorA1.setActive(inputs.getOrDefault("I0.2", false));
        sensorA2.setActive(inputs.getOrDefault("I0.3", false));
        sensorDoor.setActive(inputs.getOrDefault("I0.5", false));

        // Verificação de falha - Idêntico à garagem
        if (controller.motorDamaged && !errorShown && onCriticalFailureCallback != null) {
            errorShown = true;
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
        errorShown = false;
        repaint();
    }

    @Override
    public void stop() {
        controller.stopCabinUp();
        controller.stopCabinDown();
        controller.stopClosingDoors();
        controller.stopOpeningDoors();
    }

    @Override
    public void setInputListener(InputEventListener listener) {
        this.inputListener = listener;

        // Listener protegido para os botões de chamada (verifica se o motor vai queimar)
        InputEventListener protectedListener = new InputEventListener() {
            @Override
            public void onPressed(String inputKey, java.awt.event.MouseEvent evt) {
                if (HomePageModel.isRunning() && controller.isMotorRunning() && !controller.isDoorFullyClosed()) {
                    controller.triggerDamage();
                } else if (listener != null) {
                    listener.onPressed(inputKey, evt);
                }
            }

            @Override
            public void onReleased(String inputKey, java.awt.event.MouseEvent evt) {
                if (listener != null) listener.onReleased(inputKey, evt);
            }
        };

        btnCallA1.setInputEventListener(protectedListener);
        btnCallA2.setInputEventListener(protectedListener);

        // --- 🔴 NOVO LISTENER: EMERGÊNCIA COM TRAVA (COGUMELO) 🔴 ---
        btnEmergency.setInputEventListener(new InputEventListener() {
            @Override
            public void onPressed(String inputKey, java.awt.event.MouseEvent evt) {
                // Lemos o estado atual na memória do CLP e invertemos (Toggle)
                boolean estadoAtual = Models.HomePageModel.getInputs().getOrDefault("I0.6", true);
                Models.HomePageModel.getInputs().put("I0.6", !estadoAtual);
            }

            @Override
            public void onReleased(String inputKey, java.awt.event.MouseEvent evt) {
                // MAGIA AQUI: Nós não avisamos o sistema que o mouse foi solto!
                // Assim, o CLP entende que o botão continua pressionado até você clicar nele de novo.
            }
        });
    }

    @Override
    public void setOnCriticalFailureCallback(Runnable callback) {
        this.onCriticalFailureCallback = callback;
    }
}