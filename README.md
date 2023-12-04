# OOAD Lab 3 弹球游戏 Bizmoball

# 1. 需求分析
## 1.1 功能性需求：
- 游戏布局为三个区域，分别为游戏棋盘、图形工具、操作工具。
- 游戏分为“布局模式”和“游玩模式”两种模式，需要能在两种模式间切换。
- 布局模式的功能包括：棋盘位置选择、图形创建、图形操作选择。
- 游戏发生在棋盘上，长与宽至少各占20个单位格，单位格长度自定，图形组建只能在方格内被创建，不能放在交点上。
- 组件包含：基础图形（圆、三角、方）吸收器、轨道、挡板。
- 工具包含：旋转、删除、放大、缩小。
- 小球：游戏开始时会因为重力自由落体，遇到边界、三角形、圆形、方形、管道外壁、挡板时，实现反弹。
- 黑洞（吸收器）：小球碰到后从游戏棋盘上消失，自动进入布局模式。
- 管道：分直管道与弯管道，弯管道负责拐弯，管道内不受碰撞与重力的影响，保持匀速运动，直到抵达管道的另一端。
- 挡板初始长度为2单位，宽度为1/4单位，在游玩模式下可以实现挡板的上下拨动，在设计模式中实现挡板左右平移。
- 在游玩模式下，左右挡板可以通过键盘的操作实现左右移动。
- 文件系统：包括新建游戏、保存游戏、读取游戏三个功能。
### 1.1.1 绑定的按键行为
1. 单击：选中、拖动简单行为。
2. 鼠标滚轮：设计模式中选中某个组件，通过鼠标滚轮控制组件放大缩小。
3. 左右方向键 和 A、D键：游戏模式中，分别控制左右挡板是否拨动。
4. 左右方向键 和 WASD键：设计模式中，控制选中组件上下左右移动。
5. ENTER键：切换设计模式 or 游戏模式。
6. Delete：设计模式中选中某个组件，按Delete删除。
## 1.2 非功能性需求：
- 用户在模式之间进行切换时，响应速度要快
- 在游玩模式下，游戏要保证运行的流畅

# 2 系统设计
## 2.1 游戏引擎
游戏引擎分为如下几个部分：参数设置、抽象游戏世界、坐标系、物体、碰撞处理。
### 2.1.1 参数设置
在Setting.java文件中，定义与物理引擎和游戏逻辑相关的参数以及他们的默认数值。
- DEFAULT_ANGULAR_DAMPING: 用于模拟物体旋转时的阻尼效果。表示每秒钟角速度减小的比例。
- TICKS_PER_SECOND: 游戏引擎每秒更新的次数，也称为Tick，影响游戏的实时性和平滑性。
- DEFAULT_TICK_FREQUENCY: 每个Tick的时长，以秒为单位，计算方式是 1 / TICKS_PER_SECOND。
- DEFAULT_MAXIMUM_TRANSLATION: 每个Tick对象最大的平移距离。可能用于限制物体在一个时间步内移动的最大距离。
- DEFAULT_MAXIMUM_ROTATION: 每个Tick对象最大的旋转角度。可能用于限制物体在一个时间步内旋转的最大角度。
- DEFAULT_SOLVER_ITERATIONS: 在碰撞和物理求解中使用的迭代次数。这个设置可以影响模拟的准确性和性能。
- DEFAULT_LINEAR_TOLERANCE: 线性容差是一个用于确定何时认为两个物体的位置足够接近，无需进一步调整的阈值。如果物体位置变化在这个范围内，可能不会进行校正。
- DEFAULT_MAXIMUM_LINEAR_CORRECTION: 用于防止物体位置过冲的最大线性校正值。超过这个值的位置校正将被截断。
- DEFAULT_BAUMGARTE: 用于防止物体位置和旋转过冲的比例因子。它调整了误差的影响，以避免系统在一次迭代中过于激进地进行校正。
### 2.1.2 抽象游戏世界
  在AbstractWorld.java中，定义了一个抽象的世界类，用于描述整个弹球游戏中的物体所处的虚拟环境。整个弹球世界可以抽象成三个主要部分：物体、重力和碰撞。弹球、障碍物等都是物体，游戏开始后，弹球会受重力的作用下落，下落的过程中，可能会和别的物体产生碰撞从而改变轨迹。
- 定义重力：
    - EARTH_GRAVITY: 定义了一个静态的地球重力向量，表示在缺少其他引力时，物体所受到的默认重力。这个向量的值是 (0, -9.8)，意味着竖直向下的加速度是 9.8 米/秒²。
    - gravity: 一个表示当前世界中的重力向量的实例变量，用于描述物体在该世界中受到的重力影响。
- 定义物体
    - Bodies: 一个列表，用于存储该世界中所有物理体的实例。
    - addBody: 用于向世界添加物体的方法。
    - removeBody: 用于向世界移除单个物体的方法。
    - removeAllBodies: 用于向世界移除所有物体的方法。
    - getBodies: 返回世界中所有物体的列表。
- 定义碰撞
    - collisionDetector: 一个碰撞检测器的实例，用于检测物体之间的碰撞。
    - solver: 一个碰撞解算器的实例，用于处理碰撞后的物体行为
### 2.1.3 物体
在Physics中包含了与物体相关的多个类。
- Mass 类：是一个简单的数据类，用于存储和计算与质量相关的参数。描述物体的质量和转动惯量等属性。
- MassType 类：是一个枚举类，表示物体的质量类型，包括正常质量和无限大的质量，提供了一个清晰的方式来标识物体的质量特性。
- PhysicsBody 类：代表一个物体，可能包含多个基础物体或者是一个独立的物体。包含了质量、速度、阻尼、受力等多个属性，以及用于模拟物体整体行为的方法。
### 2.1.4 坐标系
  在Geometry中包含与坐标、位置和物体形状相关的各个类。
- Vector2 类：提供了一系列用于处理二维向量的实用方法，涵盖了向量的基本操作、运算、几何计算等多个方面。
- XXYY类：该类主要用于表示轴对齐的边界框，并提供了判断两个边界框是否重叠以及平移边界框的功能。
- Transform 类：用于表示物体的旋转和位移变换。
- shape：用于表示系统中的所有物体的形状。在抽象类AbstractShape中定义了物体的基本属性和物体进行旋转、平移、缩放等基本操作的方法，所有形状都需要继承AbstractShape。
### 2.1.5 碰撞
  Collision部分主要用来处理两个物体发生碰撞。
  处理碰撞可以分为两步：碰撞检测和碰撞解析。
  碰撞检测相关的功能在CollisionDetector下实现。
  碰撞检测的主要步骤如下：
1. 宽碰撞检测：最粗粒度的碰撞检测，只判断两个AbstractShape的轴对齐边界框（XXYY）是否发生碰撞。如果两个物体的边界框都没有发生碰撞，则可以直接判定两个物体没有发生碰撞。
2. 窄碰撞检测：在窄碰撞检测中，需要根据物体形状的不同，判断两个物体是否会发生碰撞。比如，两个圆形就需要比较圆心之间的距离和半径的和。
3. 分离轴算法：使用分离轴定理（SAT）算法判断两个物体是否发生碰撞，针对不同类型的形状，使用不同的方法计算分离轴。该算法基于以下原理：如果两个凸多边形不相交，那么存在一条直线（分离轴），沿该直线的投影在至少一个多边形上是不相交的。如果两个凸多边形相交，那么在所有可能的分离轴上都存在投影重叠。
   碰撞解析相关的功能在Contact和Manifold下实现。
1. 创建碰撞约束并进行初始化：在窄碰撞检测后，为每对碰撞的物体创建碰撞约束，包括包含接触物体对、接触信息、法线、切线、阻力系数、回弹系数等信息。
2. 计算碰撞流形：使用Manifold对象来表示碰撞的性质，包括碰撞点的位置和分离法线的方向。使用Solver求解器，可以将将穿透信息解析为碰撞流形。计算碰撞流形的目的是尽可能精确地找到碰撞点，并生成一个描述碰撞信息的Manifold对象。
3. 速度求解器：使用迭代方法，解决速度相关的碰撞约束，包括施加冲量、处理摩擦等。在SequentialImpulses类包含solveVelocityConstraints方法，用于解决速度相关的碰撞约束。
4. 位置求解器：解决位置相关的碰撞约束，处理物体的位移和旋转。在SequentialImpulses:中包含solvePositionConstraints方法，用于解决位置相关的碰撞约束。
## 2.2 UI显示功能划分
   整个游戏UI界面分为三大部分：①网格区展示游戏面板；②操作区展示游戏操作(切换模式/删除/上下左右移动)；③组件区显示游戏组件(基础图形/球/挡板等)。
1. 操作面板展示原理：用fxml文件规定UI布局、等展示参数。通过fx:id唯一标识符指定元素，从而能够在控制器中引用fxml中的设置。在启动类中，通过@FXML注入FXML文件，实现在PlayerPanel中实现对这些组件的控制。利用json文件为媒介存储数据，实现导入导出。
2. error类消息提示(e.g.越界/已最小...)：使用弹框显示，动画效果为显示-->淡出(通过线程实现)，控制弹框展示时间。
3. 按键及鼠标/触摸板手势操作原理：绑定系统监听事件，由javafx.scene库提供，有MouseClicked、KeyPressed、KeyReleased。
## 2.3 文件系统导入导出
   基本原理为：将所有可移动组件封装为GizmoPhysicsBody，从而达到统一。利用json文件存储格子面板中所有物体对象的属性信息(包括坐标信息)。例如一个左挡板的json数据有如下，即该对象包含的所有属性。
   利用PersistentUtil.fromJsonString()方法，将json数据转换为对象PhysicsBody，然后再利用addBodyToGrid()方法，将所有对象展示到对应位置显示。
## 2.4 领域层设计
领域层主要实现实现AbstractWorld中的游戏运行环境，包括以下三个板块的功能：
### 2.4.1 物体（PhysicsBody）
   基于geometry.shape，将抽象图形具体化为游戏当中需要用到的实际物体，并且实现了初始化函数和相关操作函数。
1. Ball、BlackHole和ObstacleCircle
   继承自geometry.shape.Circle，包含三个初始化函数，允许游戏世界使用无参数/只有半径参数/有半径和位移旋转信息这三种方式添加该物体。
2. CurvedPipe
   继承自geometry.shape.QuarterCircle，包含三个初始化函数，允许游戏世界使用无参数/只有半径参数/有半径和位移旋转信息这三种方式添加该物体。
3. Flipper
   继承自geometry.shape.Triangle，包含三个初始化函数，分别在无参数（默认方向朝下）/只规定方向和位置/规定方向、位置、旋转位移的情况下初始化挡板实体。
   定义rise和down函数，用于规定挡板运动方向。
   定义flip函数，用于旋转挡板规定角度。
4. ObstacleRectangle
   继承自geometry.shape.Rectangle，包含两个初始化函数，分别在只有半长、半宽/有半长、半宽、旋转位移信息的情况下初始化障碍物矩形实体。
5. ObstacleTriangle
   继承自geometry.shape.Triangle，包含两个初始化函数，都需要三角形的顶点位置信息，旋转位移信息作为可选。
6. Pipe
   继承自geometry.shape.Rectangle，包含两个初始化函数，分别在只有半长、半宽/有半长、半宽、旋转位移信息的情况下初始化障碍物矩形实体，默认方向为水平。
   定义rotate函数，可用于改变管道方向（水平/竖直）。

### 2.4.2 碰撞过滤器（Filter）
碰撞过滤器用于防止系统将球在管道中穿行的情况识别为球与管道发生碰撞，导致球被弹开。
分为三个阶段：
- isAllowedBroadPhase：粗略筛查。
- isAllowedNarrowPhase：精确检测。
- isAllowedManifold：根据球和弯管的形状、位置、方向信息判断球和弯管的相对位置关系（内部/外部/边缘），返回布尔值表示是否允许碰撞发生。
1. CurvedPipeCollisionFilter
   处理球和弯管之间的碰撞过滤，判断其是否能发生碰撞。
   该方法通过弯管的三个顶点和球中心点的向量，以及弯管两条边的向量，判断球的中心点是否在弯管内部，即是否在弯管的两条边所构成的扇形区域内。
   若球的中心点在弯管内部，判断球的边缘是否与弯管的两个端点相交，是则返回true，允许碰撞。否则不允许碰撞。
   若球中心点在弯管半径内，判断球的边缘是否与弯管的两个端点相交，是则返回true，表示允许碰撞。判断穿透的法线方向是否与弯管的两条边的方向垂直，是则返回false，表示不允许碰撞。
   若球的中心点不在上述两种情况中，判断球的中心点到弯管的两条边的投影长度是否小于等于弯管的两条边的长度，如果是，返回false，表示不允许碰撞。
2. PipeCollisionFilter
   根据球和管道的形状、位置、方向信息判断球和弯管的相对位置关系（内部/外部/边缘），返回布尔值表示是否允许碰撞发生。
   该类中主要实现fixCollision方法，用于修正球和管道之间碰撞的结果。获取穿透的法线方向和深度，以及球和管道的位置、方向等信息，重新计算穿透的深度，并将法线方向设为与管道的边缘垂直的方向。
   该类中maintainPipeProperty方法主要负责维持弯管的物理特性，判断球和弯管的旋转速率是否相同，如果相同，根据弯管的方向，将球的线速度在相应的轴上设为零，使球沿着弯管的弧线运动。如果球的线速度小于30，将球的线速度放大到30，使球不会因为速度过小而停留在弯管内。
### 2.4.3 逐帧回调函数（TickListener）
   该函数每帧自动触发，用于处理不同物体之间碰撞的效果。
1. BallListener
   用于在每一帧中调用碰撞检测器，检测所有球之间的碰撞，并返回一个包含碰撞流形和碰撞物体对的列表。
   detect方法对每一对球进行碰撞检测，对processDetect传入碰撞流形求解器和两个球对象作为参数。如果processDetect方法返回了一个非空的碰撞流形，说明两个球发生了碰撞，将碰撞流形和碰撞物体对作为一个元组添加到列表中。
2. BlackHoleListener
   对每一对球和黑洞进行碰撞检测，调用私有方法gravityAccumulation，用于计算球受到黑洞的引力作用。调用DetectorUtil.circleDetect，用于判断球和黑洞是否相交。若球和黑洞相交，说明发生了碰撞，将一个空的碰撞流形和碰撞物体对作为一个元组添加到列表中。对于每一对发生碰撞的球和黑洞，将球从球的列表和所有物体的列表中移除，表示球被黑洞吞噬
   gravityAccumulation方法计算球到黑洞的距离，根据万有引力公式，计算引力的大小，与引力的方向相乘，得到引力的向量，并获取球的质量，将引力的大小乘以球的质量，表示引力对球的作用力，将引力作为一个力添加到球的力列表中。
3. FlipperListener
   每一帧都更新挡板位置并进行碰撞检测，根据挡板的形状角度信息，判断挡板是否处于上升/下降状态，设置角速度和要调整的角度。
   该类含有setUpVelocity、setDownVelocity和updatePosition方法，用于改变挡板的角度和线速度。
4. ObstacleListner
   该类用于在每一帧中监听球和遮挡物之间的碰撞事件。
5. PipeListener
   该类用于在每一帧中监听球和管道之间的碰撞事件。
## 2.5 游戏世界
   继承了AbstractWorld，是游戏场景的控制类。在该类中，创建了一个用于存储不同类型物理实体的哈希列表，并且使用列表存储了适用于不同物理实体类型的碰撞监听器。
   该类实现了向游戏场景中添加物理实体/移除物理实体/移除所有物理实体的功能。
   该类负责在每帧调用各个逐帧回调函数用于检测碰撞情况。