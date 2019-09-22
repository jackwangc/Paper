
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

class Ant implements Cloneable {

    private Vector<Integer> tabu; // 禁忌表
    private Vector<Integer> allowedCities; // 允许搜索的城市
    private float[][] delta; // 信息数变化矩阵
    private int[][] distance; // 距离矩阵
    private float alpha;
    private float beta;

    private int tourLength; // 路径长度
    private int cityNum; // 城市数量
    private int firstCity; // 起始城市
    private int currentCity; // 当前城市

    public Ant() {
        cityNum = 30;
        tourLength = 0;
    }

    /**
     * Constructor of Ant
     *
     * @param num
     *            蚂蚁数量
     */
    public Ant(int num) {
        cityNum = num;
        tourLength = 0;
    }

    /**
     * 初始化蚂蚁，随机选择起始位置
     *
     * @param distance
     *            距离矩阵
     * @param a
     *            alpha
     * @param b
     *            beta
     */

    public void init(int[][] distance, float a, float b) {
        alpha = a;
        beta = b;
        // 初始允许搜索的城市集合
        allowedCities = new Vector<Integer>();
        // 初始禁忌表
        tabu = new Vector<Integer>();
        // 初始距离矩阵
        this.distance = distance;
        // 初始信息数变化矩阵为0
        delta = new float[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            Integer integer = new Integer(i);
            allowedCities.add(integer);
            for (int j = 0; j < cityNum; j++) {
                delta[i][j] = 0.f;
            }
        }
        // 随机挑选一个城市作为起始城市
        Random random = new Random(System.currentTimeMillis());
        firstCity = random.nextInt(cityNum);
        // 允许搜索的城市集合中移除起始城市
        for (Integer i : allowedCities) {
            if (i.intValue() == firstCity) {
                allowedCities.remove(i);
                break;
            }
        }
        // 将起始城市添加至禁忌表
        tabu.add(Integer.valueOf(firstCity));
        // 当前城市为起始城市
        currentCity = firstCity;
    }

    /**
     *
     * 选择下一个城市
     *
     * @param pheromone
     *            信息素矩阵
     */

    public void selectNextCity(float[][] pheromone) {
        float[] p = new float[cityNum];
        float sum = 0.0f;
        // 计算分母部分
        for (Integer i : allowedCities) {
            sum += Math.pow(pheromone[currentCity][i.intValue()], alpha)
                    * Math.pow(1.0 / distance[currentCity][i.intValue()], beta);
        }
        // 计算概率矩阵
        for (int i = 0; i < cityNum; i++) {
            boolean flag = false;
            for (Integer j : allowedCities) {
                if (i == j.intValue()) {
                    p[i] = (float) (Math.pow(pheromone[currentCity][i], alpha) * Math
                            .pow(1.0 / distance[currentCity][i], beta)) / sum;
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                p[i] = 0.f;
            }
        }
        // 轮盘赌选择下一个城市
        Random random = new Random(System.currentTimeMillis());
        float sleectP = random.nextFloat();
        int selectCity = 0;
        float sum1 = 0.f;
        for (int i = 0; i < cityNum; i++) {
            sum1 += p[i];
            if (sum1 >= sleectP) {
                selectCity = i;
                break;
            }
        }
        // 从允许选择的城市中去除select city
        for (Integer i : allowedCities) {
            if (i.intValue() == selectCity) {
                allowedCities.remove(i);
                break;
            }
        }
        // 在禁忌表中添加select city
        tabu.add(Integer.valueOf(selectCity));
        // 将当前城市改为选择的城市
        currentCity = selectCity;
    }

    /**
     * 计算路径长度
     *
     * @return 路径长度
     */
    private int calculateTourLength() {
        int len = 0;
        //禁忌表tabu最终形式：起始城市,城市1,城市2...城市n,起始城市
        for (int i = 0; i < cityNum; i++) {
            len += distance[this.tabu.get(i).intValue()][this.tabu.get(i + 1)
                    .intValue()];
        }
        return len;
    }

    public Vector<Integer> getAllowedCities() {
        return allowedCities;
    }

    public void setAllowedCities(Vector<Integer> allowedCities) {
        this.allowedCities = allowedCities;
    }

    public int getTourLength() {
        tourLength = calculateTourLength();
        return tourLength;
    }

    public void setTourLength(int tourLength) {
        this.tourLength = tourLength;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public Vector<Integer> getTabu() {
        return tabu;
    }

    public void setTabu(Vector<Integer> tabu) {
        this.tabu = tabu;
    }

    public float[][] getDelta() {
        return delta;
    }

    public void setDelta(float[][] delta) {
        this.delta = delta;
    }

    public int getFirstCity() {
        return firstCity;
    }

    public void setFirstCity(int firstCity) {
        this.firstCity = firstCity;
    }

}

class ACO {

    private Ant[] ants; // 蚂蚁
    private int antNum; // 蚂蚁数量
    private int cityNum; // 城市数量
    private int MAX_GEN; // 运行代数
    private float[][] pheromone; // 信息素矩阵
    private int[][] distance; // 距离矩阵
    private int bestLength; // 最佳长度
    private int[] bestTour; // 最佳路径

    // 三个参数
    private float alpha;
    private float beta;
    private float rho;

    public ACO() {

    }

    /**
     * constructor of ACO
     *
     * @param n
     *            城市数量
     * @param m
     *            蚂蚁数量
     * @param g
     *            运行代数
     * @param a
     *            alpha
     * @param b
     *            beta
     * @param r
     *            rho
     *
     **/
    public ACO(int n, int m, int g, float a, float b, float r) {
        cityNum = n;
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        alpha = a;
        beta = b;
        rho = r;
    }

    // 给编译器一条指令，告诉它对被批注的代码元素内部的某些警告保持静默
    @SuppressWarnings("resource")
    /**
     * 初始化ACO算法类
     * @param filename 数据文件名，该文件存储所有城市节点坐标数据
     * @throws IOException
     */
    private void init(String filename) throws IOException {
        // 读取数据
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        distance = new int[cityNum][cityNum];
        x = new int[cityNum];
        y = new int[cityNum];
        for (int i = 0; i < cityNum; i++) {
            // 读取一行数据，数据格式1 6734 1453
            strbuff = data.readLine();
            // 字符分割
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);// x坐标
            y[i] = Integer.valueOf(strcol[2]);// y坐标
        }
        // 计算距离矩阵
        // 针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
        for (int i = 0; i < cityNum - 1; i++) {
            distance[i][i] = 0; // 对角线为0
            for (int j = i + 1; j < cityNum; j++) {
                double rij = Math
                        .sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                                * (y[i] - y[j])) / 10.0);
                // 四舍五入，取整
                int tij = (int) Math.round(rij);
                if (tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }
        distance[cityNum - 1][cityNum - 1] = 0;
        // 初始化信息素矩阵
        pheromone = new float[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = 0.1f; // 初始化为0.1
            }
        }
        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum + 1];
        // 随机放置蚂蚁
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distance, alpha, beta);
        }
    }

    public void solve() {
        // 迭代MAX_GEN次
        for (int g = 0; g < MAX_GEN; g++) {
            // antNum只蚂蚁
            for (int i = 0; i < antNum; i++) {
                // i这只蚂蚁走cityNum步，完整一个TSP
                for (int j = 1; j < cityNum; j++) {
                    ants[i].selectNextCity(pheromone);
                }
                // 把这只蚂蚁起始城市加入其禁忌表中
                // 禁忌表最终形式：起始城市,城市1,城市2...城市n,起始城市
                ants[i].getTabu().add(ants[i].getFirstCity());
                // 查看这只蚂蚁行走路径距离是否比当前距离优秀
                if (ants[i].getTourLength() < bestLength) {
                    // 比当前优秀则拷贝优秀TSP路径
                    bestLength = ants[i].getTourLength();
                    for (int k = 0; k < cityNum + 1; k++) {
                        bestTour[k] = ants[i].getTabu().get(k).intValue();
                    }
                }
                // 更新这只蚂蚁的信息数变化矩阵，对称矩阵
                for (int j = 0; j < cityNum; j++) {
                    ants[i].getDelta()[ants[i].getTabu().get(j).intValue()][ants[i]
                            .getTabu().get(j + 1).intValue()] = (float) (1. / ants[i]
                            .getTourLength());
                    ants[i].getDelta()[ants[i].getTabu().get(j + 1).intValue()][ants[i]
                            .getTabu().get(j).intValue()] = (float) (1. / ants[i]
                            .getTourLength());
                }
            }
            // 更新信息素
            updatePheromone();
            // 重新初始化蚂蚁
            for (int i = 0; i < antNum; i++) {
                ants[i].init(distance, alpha, beta);
            }
        }

        // 打印最佳结果
        printOptimal();
    }

    // 更新信息素
    private void updatePheromone() {
        // 信息素挥发
        for (int i = 0; i < cityNum; i++)
            for (int j = 0; j < cityNum; j++)
                pheromone[i][j] = pheromone[i][j] * (1 - rho);
        // 信息素更新
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                for (int k = 0; k < antNum; k++) {
                    pheromone[i][j] += ants[k].getDelta()[i][j];
                }
            }
        }
    }

    private void printOptimal() {
        System.out.println("The optimal length is: " + bestLength);
        System.out.println("The optimal tour is: ");
        for (int i = 0; i < cityNum + 1; i++) {
            System.out.println(bestTour[i]);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Start....");
        ACO aco = new ACO(48, 10, 100, 1.f, 5.f, 0.5f);
        aco.init("c://data.txt");
        aco.solve();
    }

}


