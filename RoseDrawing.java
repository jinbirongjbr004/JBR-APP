import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class RoseDrawing extends JFrame {
    
    private int numRoses = 1; // 初始玫瑰花数量
    
    public RoseDrawing() {
        setTitle("Java玫瑰花绘制");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建控制面板
        JPanel controlPanel = new JPanel();
        JButton addButton = new JButton("添加玫瑰花");
        JButton removeButton = new JButton("减少玫瑰花");
        JButton changeColorButton = new JButton("更换颜色");
        JButton animateButton = new JButton("花瓣动画");
        JButton fallingPetalsButton = new JButton("花瓣飘落");
        
        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        controlPanel.add(changeColorButton);
        controlPanel.add(animateButton);
        controlPanel.add(fallingPetalsButton);
        
        // 创建绘图面板
        RosePanel rosePanel = new RosePanel();
        
        // 按钮事件
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                numRoses++;
                rosePanel.setNumRoses(numRoses);
                rosePanel.repaint();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (numRoses > 1) {
                    numRoses--;
                    rosePanel.setNumRoses(numRoses);
                    rosePanel.repaint();
                }
            }
        });
        
        changeColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rosePanel.changeColors();
                rosePanel.repaint();
            }
        });
        
        animateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rosePanel.toggleAnimation();
            }
        });
        
        fallingPetalsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rosePanel.toggleFallingPetals();
            }
        });
        
        // 添加组件到窗口
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(rosePanel, BorderLayout.CENTER);
    }
    
    // 飘落的花瓣类
    static class FallingPetal {
        float x, y;              // 位置
        float speedX, speedY;    // 速度
        float rotation;          // 旋转角度
        float rotationSpeed;     // 旋转速度
        float size;              // 大小
        float alpha;             // 透明度
        Color color;             // 颜色
        Path2D path;             // 花瓣形状
        
        public FallingPetal(float x, float y, Color color) {
            Random rand = new Random();
            this.x = x;
            this.y = y;
            this.color = color;
            this.speedX = rand.nextFloat() * 2 - 1;  // -1 到 1
            this.speedY = 1 + rand.nextFloat() * 2;  // 1 到 3
            this.rotation = rand.nextFloat() * 360;
            this.rotationSpeed = rand.nextFloat() * 6 - 3;  // -3 到 3
            this.size = 10 + rand.nextFloat() * 20;  // 10 到 30
            this.alpha = 0.7f + rand.nextFloat() * 0.3f;  // 0.7 到 1.0
            
            // 创建花瓣形状
            this.path = createPetalShape();
        }
        
        private Path2D createPetalShape() {
            Path2D petal = new Path2D.Float();
            petal.moveTo(0, 0);
            petal.curveTo(size/3, -size/2, size, -size/3, size/2, -size);
            petal.curveTo(0, -size/2, -size/2, -size, -size, -size/3);
            petal.curveTo(-size/3, -size/2, 0, 0, 0, 0);
            return petal;
        }
        
        public void update() {
            x += speedX;
            y += speedY;
            rotation += rotationSpeed;
            alpha -= 0.005f;  // 逐渐变透明
            
            // 添加随机摆动
            speedX += (Math.random() - 0.5) * 0.3;
            // 限制水平速度
            if (speedX > 1.5) speedX = 1.5f;
            if (speedX < -1.5) speedX = -1.5f;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            
            g2d.setColor(color);
            g2d.fill(path);
            
            // 重置变换
            g2d.rotate(-Math.toRadians(rotation));
            g2d.translate(-x, -y);
            
            // 重置合成模式
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        public boolean isDead() {
            return alpha <= 0 || y > 2000;  // 花瓣完全透明或者飘出屏幕
        }
    }
    
    // 玫瑰花绘制面板
    class RosePanel extends JPanel {
        private int numRoses = 1;
        private Color[] petalColors = {
            new Color(255, 0, 127),    // 玫瑰红
            new Color(255, 105, 180),  // 热粉红
            new Color(219, 112, 147),  // 深粉红
            new Color(199, 21, 133),   // 紫红色
            new Color(255, 20, 147)    // 深粉红
        };
        private int currentColorIndex = 0;
        
        // 颜色渐变相关
        private Color currentColor;
        private Color targetColor;
        private float colorTransition = 0f;
        private boolean isColorChanging = false;
        
        // 动画相关变量
        private Timer animationTimer;
        private boolean isAnimating = false;
        private double animationAngle = 0;
        private double sizeMultiplier = 1.0;
        private boolean sizeIncreasing = true;
        
        // 花瓣飘落相关
        private ArrayList<FallingPetal> fallingPetals = new ArrayList<>();
        private boolean isFallingPetals = false;
        private Timer petalTimer;
        private Random random = new Random();
        
        public RosePanel() {
            setBackground(Color.WHITE);
            currentColor = petalColors[currentColorIndex];
            targetColor = petalColors[currentColorIndex];
            
            // 初始化动画计时器
            animationTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 更新动画参数
                    animationAngle += 0.1;
                    if (animationAngle > 2 * Math.PI) {
                        animationAngle = 0;
                    }
                    
                    // 调整大小倍增器
                    if (sizeIncreasing) {
                        sizeMultiplier += 0.01;
                        if (sizeMultiplier >= 1.2) {
                            sizeIncreasing = false;
                        }
                    } else {
                        sizeMultiplier -= 0.01;
                        if (sizeMultiplier <= 0.8) {
                            sizeIncreasing = true;
                        }
                    }
                    
                    // 更新颜色渐变
                    if (isColorChanging) {
                        colorTransition += 0.02f;
                        if (colorTransition >= 1.0f) {
                            currentColor = targetColor;
                            isColorChanging = false;
                            
                            // 随机选择下一个目标颜色
                            if (random.nextDouble() < 0.05) { // 5%的概率切换到新颜色
                                int nextColorIndex;
                                do {
                                    nextColorIndex = random.nextInt(petalColors.length);
                                } while (nextColorIndex == currentColorIndex);
                                
                                currentColorIndex = nextColorIndex;
                                targetColor = petalColors[currentColorIndex];
                                colorTransition = 0f;
                                isColorChanging = true;
                            }
                        } else {
                            // 计算当前过渡颜色
                            int r = interpolateColor(currentColor.getRed(), targetColor.getRed(), colorTransition);
                            int g = interpolateColor(currentColor.getGreen(), targetColor.getGreen(), colorTransition);
                            int b = interpolateColor(currentColor.getBlue(), targetColor.getBlue(), colorTransition);
                            currentColor = new Color(r, g, b);
                        }
                    } else if (random.nextDouble() < 0.01) { // 1%的概率开始新的颜色变化
                        int nextColorIndex;
                        do {
                            nextColorIndex = random.nextInt(petalColors.length);
                        } while (nextColorIndex == currentColorIndex);
                        
                        currentColorIndex = nextColorIndex;
                        targetColor = petalColors[currentColorIndex];
                        colorTransition = 0f;
                        isColorChanging = true;
                    }
                    
                    // 重绘面板
                    repaint();
                }
            });
            
            // 初始化花瓣飘落计时器
            petalTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 更新现有花瓣
                    Iterator<FallingPetal> it = fallingPetals.iterator();
                    while (it.hasNext()) {
                        FallingPetal petal = it.next();
                        petal.update();
                        if (petal.isDead()) {
                            it.remove();
                        }
                    }
                    
                    // 随机生成新花瓣
                    if (isFallingPetals && random.nextDouble() < 0.2) { // 20%的概率生成新花瓣
                        int width = getWidth();
                        // 从屏幕顶部随机位置掉落
                        float x = random.nextFloat() * width;
                        
                        // 随机选择颜色，偏向当前花朵颜色
                        Color petalColor;
                        if (random.nextDouble() < 0.7) { // 70%使用当前颜色
                            petalColor = currentColor;
                        } else {
                            petalColor = petalColors[random.nextInt(petalColors.length)];
                        }
                        
                        // 稍微调整颜色使每个花瓣略有不同
                        int r = Math.min(255, Math.max(0, petalColor.getRed() + random.nextInt(41) - 20));
                        int g = Math.min(255, Math.max(0, petalColor.getGreen() + random.nextInt(41) - 20));
                        int b = Math.min(255, Math.max(0, petalColor.getBlue() + random.nextInt(41) - 20));
                        petalColor = new Color(r, g, b);
                        
                        fallingPetals.add(new FallingPetal(x, -20, petalColor));
                    }
                    
                    repaint();
                }
            });
        }
        
        // 颜色插值辅助方法
        private int interpolateColor(int start, int end, float fraction) {
            return (int)(start + (end - start) * fraction);
        }
        
        public void setNumRoses(int num) {
            this.numRoses = num;
        }
        
        public void changeColors() {
            currentColorIndex = (currentColorIndex + 1) % petalColors.length;
            currentColor = petalColors[currentColorIndex];
            targetColor = currentColor;
            isColorChanging = false;
        }
        
        public void toggleAnimation() {
            isAnimating = !isAnimating;
            if (isAnimating) {
                animationTimer.start();
            } else {
                animationTimer.stop();
                sizeMultiplier = 1.0;
                animationAngle = 0;
                repaint();
            }
        }
        
        public void toggleFallingPetals() {
            isFallingPetals = !isFallingPetals;
            if (isFallingPetals) {
                petalTimer.start();
                // 如果动画没有运行，也启动动画
                if (!isAnimating) {
                    toggleAnimation();
                }
            } else {
                petalTimer.stop();
                // 清空所有飘落的花瓣
                fallingPetals.clear();
                repaint();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int width = getWidth();
            int height = getHeight();
            
            // 计算每朵玫瑰花的位置
            int rows = (int) Math.ceil(Math.sqrt(numRoses));
            int cols = (int) Math.ceil((double) numRoses / rows);
            
            int roseWidth = width / cols;
            int roseHeight = height / rows;
            
            // 先绘制所有的玫瑰花
            for (int i = 0; i < numRoses; i++) {
                int row = i / cols;
                int col = i % cols;
                
                int centerX = col * roseWidth + roseWidth / 2;
                int centerY = row * roseHeight + roseHeight / 2;
                
                // 为每朵花添加不同的动画相位
                double phaseOffset = i * (Math.PI / numRoses);
                
                // 画玫瑰花
                drawRose(g2d, centerX, centerY, Math.min(roseWidth, roseHeight) / 3, phaseOffset);
            }
            
            // 然后绘制所有飘落的花瓣（这样花瓣会在玫瑰花之上）
            for (FallingPetal petal : fallingPetals) {
                petal.draw(g2d);
            }
        }
        
        private void drawRose(Graphics2D g2d, int centerX, int centerY, int size, double phaseOffset) {
            // 根据动画状态调整大小
            int animatedSize = (int)(size * (isAnimating ? sizeMultiplier : 1.0));
            
            // 画茎
            g2d.setColor(new Color(46, 139, 87)); // 海洋绿
            g2d.setStroke(new BasicStroke(animatedSize/10));
            g2d.drawLine(centerX, centerY + animatedSize, centerX, centerY + animatedSize*2);
            
            // 画叶子
            drawLeaf(g2d, centerX, centerY + animatedSize*3/2, animatedSize/2, -30);
            drawLeaf(g2d, centerX, centerY + animatedSize*3/2, animatedSize/2, 30);
            
            // 画花瓣 - 使用当前的渐变颜色
            g2d.setColor(currentColor);
            
            Path2D path = new Path2D.Double();
            boolean firstPoint = true;
            
            // 使用参数方程画玫瑰曲线
            // r = a * sin(k * theta) 玫瑰曲线
            int k = 4; // 花瓣数量参数
            
            // 动画状态下的旋转角度
            double animOffset = isAnimating ? (animationAngle + phaseOffset) : 0;
            
            for (double theta = 0; theta < 2 * Math.PI; theta += 0.01) {
                // 添加波动效果
                double waveEffect = isAnimating ? Math.sin(5 * theta + animationAngle) * 0.1 : 0;
                
                double r = animatedSize * (1 + waveEffect) * Math.sin(k * (theta + animOffset));
                double x = r * Math.cos(theta) + centerX;
                double y = r * Math.sin(theta) + centerY;
                
                if (firstPoint) {
                    path.moveTo(x, y);
                    firstPoint = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            
            path.closePath();
            g2d.fill(path);
            
            // 画花蕊
            g2d.setColor(Color.YELLOW);
            int centerSize = (int)(animatedSize/10 * (isAnimating ? (0.8 + Math.sin(animationAngle*2) * 0.2) : 1.0));
            g2d.fillOval(centerX - centerSize, centerY - centerSize, centerSize*2, centerSize*2);
        }
        
        private void drawLeaf(Graphics2D g2d, int x, int y, int size, double angle) {
            Color leafColor = new Color(46, 139, 87); // 海洋绿
            g2d.setColor(leafColor);
            
            Path2D leaf = new Path2D.Double();
            
            // 旋转角度转为弧度
            double radians = Math.toRadians(angle);
            
            // 动画状态下的叶子摆动
            if (isAnimating) {
                radians += Math.sin(animationAngle) * 0.1;
            }
            
            leaf.moveTo(x, y);
            
            for (double t = 0; t <= Math.PI; t += 0.1) {
                double leafX = size * Math.sin(t) * Math.cos(t);
                double leafY = size * Math.sin(t);
                
                // 应用旋转
                double rotatedX = leafX * Math.cos(radians) - leafY * Math.sin(radians);
                double rotatedY = leafX * Math.sin(radians) + leafY * Math.cos(radians);
                
                leaf.lineTo(x + rotatedX, y + rotatedY);
            }
            
            leaf.closePath();
            g2d.fill(leaf);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RoseDrawing().setVisible(true);
            }
        });
    }
} 