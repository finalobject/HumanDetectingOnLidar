原文在这 <a href="http://finalobject.cn/lucario/human_detecting_on_lidar">finalobject.cn</a>

这个项目是无人车里的一部分，完成激光雷达的驱动，数据采集然后后期的处理以及人型识别，并不涉及车辆硬件的控制。主要分三个大块讲吧，硬件驱动、数据聚类，以及模式识别，本来还想写一写UI部分的，但是感觉自己的UI写的其实蛮简陋的，所有还是不写了，对UI实现有兴趣的自己拔一下源代码吧。

github：<a href="https://github.com/finalObject/HumanDetectingOnLidar">https://github.com/finalObject/HumanDetectingOnLidar</a>

这个项目是我大二时候写的，那时候还不会用github，版本记录都是直接备份实现的，所以在源代码里就会出现很多版本的项目，以最新的为准即可。然后里面files里还有一些项目中用到的硬件驱动库，型号资料。项目里有两个main类，带有radar的是连接雷达实时显示的，不带radar的，是把官方软件储存下来的数据文件作为数据来源。两者就是数据来源不一样。

<img class="size-medium wp-image-197 aligncenter" src="http://finalobject.cn/wp-content/uploads/2018/09/MD75YLLWXSHBVMC8UP41-300x255.png" alt="" width="300" height="255" />
<p style="text-align: center;">这是程序的截图</p>

<h1>硬件驱动部分</h1>
使用的传感器是一只小型的激光雷达，型号<a id="cba5ba614517177ef8c7d1863fbbb479-dc7cc3edc663ed95973b95a4518ad8d3c17c1f73" class="js-navigation-open" title="UTM-30LX-EW" href="https://github.com/finalObject/HumanDetectingOnLidar/tree/master/flies/UTM-30LX-EW">UTM-30LX-EW</a>，用到的库直接就是官网下载的java库<a id="36f3bd52877df112bf27f218364c8b69-a305c79a43f3c5595bfcf156fb179cb07bfbe0bf" class="js-navigation-open" title="urg_java_lib" href="https://github.com/finalObject/HumanDetectingOnLidar/tree/master/flies/urg_java_lib">urg_java_lib</a>，另外files还提供了C++/VS版本的驱动库<a id="1a476c76b093ab8347bd24cf0a04a508-86a632083c0ba27f0e98c10ca60185bf3757d1ae" class="js-navigation-open" title="urg_library-1.2.0" href="https://github.com/finalObject/HumanDetectingOnLidar/tree/master/flies/urg_library-1.2.0">urg_library-1.2.0</a>。驱动激光雷达的代码封装在<a id="e556daea6b24fe0a0edbe44edff02bd0-f5fb14991e4c884cfb714201d23ccf2e7236dc6f" class="js-navigation-open" title="RadarReader.java" href="https://github.com/finalObject/HumanDetectingOnLidar/blob/master/workspace/Sep_10_Laser/src/RadarReader.java">RadarReader.java</a>中。

这种小的激光雷达一般可以通过USB连接或者网口连接，当时可能是USB连接驱动装不上吧（忘记了），选择的是网口连接，所以在代码直接输入IP地址就可以进行连接。
<pre class="lang:java decode:true ">device = new UrgDevice(new EthernetConnection());
device.connect("192.168.0.10");
RangeSensorInformation info = device.getInformation();
if(info != null){
    System.out.println("Sensor model:" + info.product);
    System.out.println("Sensor serial number:" + info.serial_number);
}else{
    System.out.println("Sensor error:" + device.what());
}
device.setCaptureMode(CaptureSettings.CaptureMode.MD_Capture_mode);
//We set the capture type to a continuous mode so we have to start the capture
device.startCapture();</pre>
然后通过
<pre class="lang:java decode:true">capData= device.capture()</pre>
这个函数就可以获取一帧数据，因为返回的CaptureData类，我写了下面的函数把它解析成了一个数组，以方便后面对于数据的使用
<pre class="lang:java decode:true ">public static int[] setDepthsFromRadar(CaptureData data)</pre>
这种激光雷达的基础工作模式其实类似于超声波测距，只不过把超声波换成了激光，然后这个传感器还会旋转。这个型号的激光雷达视场角为270度，所以它一帧的数据其实就是不同角度下，激光雷达和最近不透光障碍物之间的距离。<a id="8d777f385d3dfec8815d20f7496026dc-f0a02324ad684e8aac90292699e4cc7db0939dd9" class="js-navigation-open" title="data" href="https://github.com/finalObject/HumanDetectingOnLidar/tree/master/flies/data">data</a>这里存了用官方软件记录下来的数据，txt格式，直接打开看其实也就是一帧一帧的数据，每一帧数据用一些时间之类的标签，主要还是就是一个数组，储存着测量结果。

我这个函数返回的结果就是把一帧里，所有距离储存成一个数据作为结果返回。至此就完成了硬件的驱动和数据采集。
<h1>聚类</h1>
因为要进行模式识别把，必须要有识别的对象，如果需要识别每一帧下，这些数据哪几块是人，哪几块是墙。因为一帧数据是整个数组，首先需要在这个数组中，找出哪几个数据点是一块，属于同一个物体，这之后才能进行模式识别。这个就是聚类。上面那张程序截图里，蓝色点就是数据结果，然后聚类之后认为是同一物体的，用红线连接起来了。

这里聚类我没有用那些传统的聚类算法，原因是这个场景比较简单，为了代码效率，放弃了需要大量迭代的k均值。

场景：这里所有数据都是有顺序的，每一个物体拥有的数据点都是连续的。这些数据都是一维的。

基本分类的原则就是，当连续两个点，距离激光雷达的距离相差大于某个阈值是判定不属于同一类。这个阈值如果手动输入的话就会很鸡肋，所有需要设计一个评价标准，让程序自动选择分类效果好的阈值。

每一个类的数据，应该都非常集中的分布在同一个距离范围内，所以可以用所有类数据的标准差之和，来判断集中程度，但是光是这样不够，因为这样一来，最好的分类结果就是所有点自成一类，标准差就全是0了。所以分类结果中，类的数量也必须足够多。因此我设计一个标准，就是【标准差之和】以及【分类类的数量】加权求和，值越小分类效果越小。这样一来遍历所有阈值，选择最好分类效果的就行，这样就实现的聚类。
<h1>模式识别</h1>
用的是<a id="06e68a784a023935bb61db7620f959fe-c5f1fa6f864950b044e05f6b6d0899177ab34242" class="js-navigation-open" title="libsvm-3.21" href="https://github.com/finalObject/HumanDetectingOnLidar/tree/master/flies/libsvm-3.21">libsvm-3.21</a>这个库，官网提供了各种语言的支持。这部分内容其实很简单了，就是从每一类数据中提取有用的特征，然后开始学习就好了。

用到的特征如下：
<ol>
 	<li style="list-style-type: none">
<ol>
 	<li>数据长度。可以是数据点的数量，或者是数据起点和终点直接的物理距离（可以用距离和角度算出二维坐标，然后计算距离）</li>
 	<li><span style="font-size: 1rem;">曲率。就是把这一类数据，拟合成椭圆，计算曲率。因为人体被激光雷达扫描的部分主要就是躯干，差不多就是一个椭圆。可以用这个来筛选墙面之类曲率过小以及垃圾桶之类曲率过大的物体</span></li>
</ol>
</li>
</ol>
还有别的参数，但是我具体也说不上来了，自己扒扒代码吧。
