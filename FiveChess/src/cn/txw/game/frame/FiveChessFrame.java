package cn.txw.game.frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * 定义FiveChessFrame类继承JFrame类实现MouseListener,Runnable接口
 */
@SuppressWarnings("all")
public class FiveChessFrame extends JFrame implements MouseListener,Runnable {
    //获取当前平面分辨率，例如：1920x1080
    int swidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    int sheight = Toolkit.getDefaultToolkit().getScreenSize().height;
    BufferedImage image = null;
    //保存坐标值
    int x = 0;
    int y = 0;
    //保存下过的棋子 0-没有棋子 1-黑子 2-白子
    int[][] allChess = new int[19][19];
    // 标识当前应该是黑子还是白子
    boolean isBlack = true;
    // 控制游戏是否可以玩
    boolean canPlay = true;
    // 保存游戏信息
    String message = "黑方先行";
    // 保存最多拥有多少时间（秒）
    int maxTime = 0;
    // 做倒计时的线程类
    Thread t = new Thread(this);
    // 保存黑方与白方的剩余时间
    int blackTime = 0;
    int whiteTime = 0;
    // 保存双方剩余时间的显示信息
    String blackMessage = "无限制";
    String whiteMessage = "无限制";
    public FiveChessFrame() {
        //设置标题
        this.setTitle("五子棋游戏");
        int width = 500, height = 500;
        this.setSize(width, height);
        //设置窗体出现的位置
        this.setLocation((swidth - width) / 2, (sheight - height) / 2);
        //窗口大小不可变
        this.setResizable(false);
        //定义关闭动作
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //添加监听器
        this.addMouseListener(this);
        //窗体可见
        this.setVisible(true);
        //加载背景图片
        try {
            image = ImageIO.read(new File("image/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 启动线程
        t.start();
        t.suspend();
        // 刷新屏幕,防止开始游戏时出现无法显示的情况.
        this.repaint();
    }
    public void paint(Graphics g) {
        //双缓冲技术防止屏幕闪烁
        BufferedImage bi = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
        Graphics g2 = bi.createGraphics();
        g2.setColor(Color.BLACK);
        //绘制背景
        g2.drawImage(image, 0, 20, this);
        g2.setFont(new Font("黑体", Font.BOLD, 20));
        g2.drawString("游戏信息："+message, 130, 60);
        //输出时间信息
        g2.setFont(new Font("宋体", 0, 14));
        g2.drawString("黑方时间：" + blackMessage, 20, 485);
        g2.drawString("白方时间：" + whiteMessage, 280, 485);
        //绘制棋盘
        /*
                   X=10 Y=70
                  X=370 Y=430
                  X=370 Y=70
                 X=10 Y=430
         */
        for (int i = 0; i < 19; i++) {
            g2.drawLine(10, 70 + 20 * i, 370, 70 + 20 * i);
            g2.drawLine(10 + 20 * i, 70, 10 + 20 * i, 430);
        }
        //标注点位
        g2.fillOval(68, 128, 4, 4);
        g2.fillOval(308, 128, 4, 4);
        g2.fillOval(308, 368, 4, 4);
        g2.fillOval(68, 368, 4, 4);
        g2.fillOval(68, 248, 4, 4);
        g2.fillOval(308, 248, 4, 4);
        g2.fillOval(188, 128, 4, 4);
        g2.fillOval(188, 368, 4, 4);
        g2.fillOval(188, 248, 4, 4);
        //绘制棋子
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                //黑子
                if (allChess[i][j] == 1) {
                    int tempX = i * 20 + 10;
                    int tempY = j * 20 + 70;
                    g2.fillOval(tempX - 7, tempY - 7, 14, 14);
                }
                //白子
                if (allChess[i][j] == 2) {
                    int tempX = i * 20 + 10;
                    int tempY = j * 20 + 70;
                    g2.setColor(Color.WHITE);
                    g2.fillOval(tempX - 7, tempY - 7, 14, 14);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(tempX - 7, tempY - 7, 14, 14);
                }
            }
        }
        g.drawImage(bi,0,0,this);
    }
    //鼠标按键在组件上按下时调用。
    @Override
    public void mousePressed(MouseEvent e) {
     //   System.out.println("X=" + e.getX() + " Y=" + e.getY());
        if (canPlay) {
            x = e.getX();
            y = e.getY();
            //判断点击是否在棋盘内
            if (x >= 10 && x <= 370 && y >= 70 && y <= 430) {
                x = (x - 10) / 20;
                y = (y - 70) / 20;
                if (allChess[x][y] == 0) {
                    if (isBlack) {
                        allChess[x][y] = 1;
                        isBlack = false;
                        message="轮到白方";
                    } else {
                        allChess[x][y] = 2;
                        isBlack = true;
                        message="轮到黑方";
                    }
                    //判断游戏是否结束
                    boolean isWin = this.checkWin();
                    if (isWin) {
                        JOptionPane.showMessageDialog(this, "游戏结束,"
                                + (allChess[x][y] == 1 ? "黑方" : "白方") + "获胜！");
                        canPlay = false;
                    }
                }
                this.repaint();
            }
        }
        // 点击 【开始游戏】 按钮
        if(e.getX() >=400 && e.getX()<=470 && e.getY()>=70 && e.getY()<=100){
            int result = JOptionPane.showConfirmDialog(this,"是否重新开始游戏？","",0);
            if(result==0){
                //1.清空棋盘
                allChess = new int[19][19];
                //另一种方式：
                /*for (int i = 0; i < 19; i++) {
                    for (int j = 0; j < 19; j++) {
                        allChess[i][j] = 0;
                    }
                }*/
                //2.重置游戏信息
                message = "黑方先行";
                //3.将下一步下棋的人改为黑方
                isBlack = true;
                //4.可以游戏标志改为true
                canPlay = true;
                //5.重置黑白双方时间限制
                blackTime = maxTime;
                whiteTime = maxTime;
                if (maxTime > 0) {
                    blackMessage = maxTime / 3600 + ":"
                            + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                            + (maxTime - maxTime / 60 * 60);
                    whiteMessage = maxTime / 3600 + ":"
                            + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                            + (maxTime - maxTime / 60 * 60);
                    t.resume();
                } else {
                    blackMessage = "无限制";
                    whiteMessage = "无限制";
                }
                //6.重新绘制窗体
                this.repaint();
            }
        }
        //点击 【游戏设置】 按钮
        if (e.getX() >= 400 && e.getX() <= 470 && e.getY() >= 120
                && e.getY() <= 150) {
            String input = JOptionPane
                    .showInputDialog("请输入游戏的最大时间(单位:分钟),如果输入0,表示没有时间限制:");
            try {
                maxTime = Integer.parseInt(input) * 60;
                if (maxTime < 0) {
                    JOptionPane.showMessageDialog(this, "请输入正确信息,不允许输入负数!");
                }
                if (maxTime == 0) {
                    int result = JOptionPane.showConfirmDialog(this,
                            "设置完成,是否重新开始游戏?");
                    if (result == 0) {
                        for (int i = 0; i < 19; i++) {
                            for (int j = 0; j < 19; j++) {
                                allChess[i][j] = 0;
                            }
                        }
                        // 另一种方式 allChess = new int[19][19];
                        message = "黑方先行";
                        isBlack = true;
                        blackTime = maxTime;
                        whiteTime = maxTime;
                        blackMessage = "无限制";
                        whiteMessage = "无限制";
                        this.canPlay = true;
                        this.repaint();
                    }
                }
                if (maxTime > 0) {
                    int result = JOptionPane.showConfirmDialog(this,
                            "设置完成,是否重新开始游戏?");
                    if (result == 0) {
                        for (int i = 0; i < 19; i++) {
                            for (int j = 0; j < 19; j++) {
                                allChess[i][j] = 0;
                            }
                        }
                        // 另一种方式 allChess = new int[19][19];
                        message = "黑方先行";
                        isBlack = true;
                        blackTime = maxTime;
                        whiteTime = maxTime;
                        blackMessage = maxTime / 3600 + ":"
                                + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                                + (maxTime - maxTime / 60 * 60);
                        whiteMessage = maxTime / 3600 + ":"
                                + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                                + (maxTime - maxTime / 60 * 60);
                        t.resume();
                        this.canPlay = true;
                        this.repaint();
                    }
                }
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(this, "请正确输入信息!");
            }
        }
        //点击 【游戏说明】 按钮
        if(e.getX() >=400 && e.getX()<=470 && e.getY()>=170 && e.getY()<=200){
            JOptionPane.showMessageDialog(this,"这是一个五子棋游戏程序，黑白双方轮流下棋，当某一方连到五子时游戏结束。");
        }
        //点击 【认输】 按钮
        if(e.getX() >=400 && e.getX()<=470 && e.getY()>=270 && e.getY()<=300){
            int result = JOptionPane.showConfirmDialog(this,"是否确认认输？","",0);
            if(result==0){
                if(isBlack){
                    JOptionPane.showMessageDialog(this,"黑方已经认输，游戏结束！");
                }else{
                    JOptionPane.showMessageDialog(this,"白方已经认输，游戏结束！");
                }
                //停止游戏
                canPlay = false;
            }
        }
        //点击 【关于】 按钮
        if(e.getX() >=400 && e.getX()<=470 && e.getY()>=320 && e.getY()<=350){
            JOptionPane.showMessageDialog(this,"本游戏由张钰制作，有问题请联系张钰。");
        }
        //点击 【退出】 按钮
        if(e.getX() >=400 && e.getX()<=470 && e.getY()>=370 && e.getY()<=400){
            JOptionPane.showMessageDialog(this,"游戏结束！");
            System.exit(0);
        }
    }
    private boolean checkWin() {
        boolean flag = false;
        //统计相连棋子数
        //横向
        int count = 1;
        int color = allChess[x][y];
        /*int i = 1;
        while (color == allChess[x + i][y]) {
            count++;
            i++;
        }
        i = 1;
        while (color == allChess[x - i][y]) {
            count++;
            i++;
        }
        if (count >= 5) {
            flag = true;
        }
        //竖向
        int count2 = 1;
        int i2 = 1;
        while (color == allChess[x][y+i2]) {
            count2++;
            i2++;
        }
        i2 = 1;
        while (color == allChess[x][y-i2]) {
            count2++;
            i2++;
        }
        if (count2 >= 5) {
            flag = true;
        }
        // 右上+左下
        int count3 = 1;
        int i3 = 1;
        while (color == allChess[x+i3][y-i3]) {
            count3++;
            i3++;
        }
        i3 = 1;
        while (color == allChess[x-i3][y+i3]) {
            count3++;
            i3++;
        }
        if (count3 >= 5) {
            flag = true;
        }
        // 左上+右下
        int count4 = 1;
        int i4 = 1;
        while (color == allChess[x-i4][y-i4]) {
            count4++;
            i4++;
        }
        i4 = 1;
        while (color == allChess[x+i4][y+i4]) {
            count4++;
            i4++;
        }
        if (count4 >= 5) {
            flag = true;
        }
*/      //判断横向
        count = this.checkCount(1,0,color);
        if(count>=5){
            flag = true;
        }else {
            //判断纵向
            count = this.checkCount(0,1,color);
            if(count>=5){
                flag = true;
            }else {
                //判断右上、左下
                count = this.checkCount(1, -1, color);
                if (count >= 5) {
                    flag = true;
                } else {
                    //判断右下、左上
                    count = this.checkCount(1, 1, color);
                    if (count >= 5) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }
    private int checkCount(int xChange, int yChange, int color){
        int count = 1;
        int tempX = xChange;
        int tempY = yChange;
        while ((x+xChange>=0 && x+xChange <= 18 && y+yChange>=0 && y+yChange <= 18 )&&color == allChess[x+xChange][y+yChange]){
            count++;
            if(xChange != 0){
                xChange++;
            }
            if(yChange!=0){
                if(yChange>0){
                    yChange++;
                }else {
                    yChange--;
                }
            }
        }
        xChange = tempX;
        yChange = tempY;
        while ((x-xChange>=0 && x-xChange <= 18 && y-yChange>=0 && y-yChange <= 18 )&&color == allChess[x-xChange][y-yChange]){
            count++;
            if(xChange != 0){
                xChange++;
            }
            if(yChange!=0){
                if(yChange>0){
                    yChange++;
                }else {
                    yChange--;
                }
            }
        }
        return count;
    }
    @Override
    public void run() {
        // 判断是否有时间限制
        if (maxTime > 0) {
            while (true) {
                if (isBlack) {
                    blackTime--;
                    if (blackTime == 0) {
                        JOptionPane.showMessageDialog(this, "黑方超时,游戏结束!");
                    }
                } else {
                    whiteTime--;
                    if (whiteTime == 0) {
                        JOptionPane.showMessageDialog(this, "白方超时,游戏结束!");
                    }
                }
                blackMessage = blackTime / 3600 + ":"
                        + (blackTime / 60 - blackTime / 3600 * 60) + ":"
                        + (blackTime - blackTime / 60 * 60);
                whiteMessage = whiteTime / 3600 + ":"
                        + (whiteTime / 60 - whiteTime / 3600 * 60) + ":"
                        + (whiteTime - whiteTime / 60 * 60);
                this.repaint();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println(blackTime + " -- " + whiteTime);
            }
        }
    }
    // 鼠标单击事件
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
}