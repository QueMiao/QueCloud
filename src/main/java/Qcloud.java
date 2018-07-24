import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;



public class Qcloud  extends JFrame implements ActionListener {
    JButton open=null;
    private JPanel contentPane;
    private JTextField textField;


    File directory = new File("");//参数为空
    String courseFile = directory.getCanonicalPath() ;


    public static void main(String[] args) throws IOException {

            Qcloud frame = new Qcloud();

    }

    File file = null;
    JFileChooser jfc = null;
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        jfc=new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
        jfc.showDialog(new JLabel(), "选择");
        file=jfc.getSelectedFile();
        if(file.isDirectory()){
            System.out.println("文件夹:"+file.getAbsolutePath());
            textField.setText(file.getAbsolutePath());
        }else if(file.isFile()){
            System.out.println("文件:"+file.getAbsolutePath());
            textField.setText(file.getAbsolutePath());
        }
        System.out.println(jfc.getSelectedFile().getName());

    }



    ClientConfig clientConfig = null;
     COSClient cosclient = null;
    String bucketName = null;
    JComboBox comboBox = null;

    JButton download_btn = null;


    JButton delbtn = null;

    public Qcloud() throws IOException {
        this.init();
        setTitle("WitQueCloud");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 415, 217);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JButton btnNewButton = new JButton("选择文件");
        btnNewButton.setBounds(251, 18, 78, 29);
        contentPane.add(btnNewButton);

        textField = new JTextField();
        textField.setBounds(6, 18, 246, 26);
        contentPane.add(textField);
        textField.setColumns(10);

        comboBox = new JComboBox();
        comboBox.setBounds(6, 101, 246, 27);
        contentPane.add(comboBox);



        delbtn = new JButton("删除");
        delbtn.setBounds(319, 100, 78, 29);
        contentPane.add(delbtn);

        JButton download_btn = new JButton("下载");
        download_btn.setBounds(251, 100, 78, 29);
        contentPane.add(download_btn);

        JButton button = new JButton("上传");
        button.setBounds(319, 16, 78, 32);
        contentPane.add(button);




        this.getFileList();

        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
               getFileList();
            }
        });



        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                upLoadFile();
            }
        });

        download_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downLoad();
            }
        });

        delbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = (String) comboBox.getSelectedItem();
                cosclient.deleteObject(bucketName, key);
                cosclient.shutdown();
                getFileList();
            }
        });

        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        btnNewButton.addActionListener((ActionListener) this);

    }




    public void init(){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDX3R2uf33h9RXZm63BLfw9SefVEmALPF3",
                "nX8YckPtCKH2d3IXcWItJmBPSJzUnwui");
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        clientConfig = new ClientConfig(new Region("ap-shanghai"));
        // 3 生成cos客户端
        cosclient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        bucketName = "tools-1251143468";
    }


    public void getFileList(){
        //获取文件列表
        comboBox.removeAllItems();

        String download = (String) comboBox.getSelectedItem();
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setMaxKeys(100);
        ObjectListing objectListing = cosclient.listObjects(listObjectsRequest);
        String nextMarker = objectListing.getNextMarker();
        boolean isTruncated = objectListing.isTruncated();
        List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        for (COSObjectSummary cosObjectSummary : objectSummaries) {
            // 文件路径
            String key = cosObjectSummary.getKey();
            // 获取文件长度
            long fileSize = cosObjectSummary.getSize();
            // 获取文件ETag
            String eTag = cosObjectSummary.getETag();
            // 获取最后修改时间
            Date lastModified = cosObjectSummary.getLastModified();
            // 获取文件的存储类型
            String StorageClassStr = cosObjectSummary.getStorageClass();
            comboBox.addItem(key);

        }
        if (download == null) {
            comboBox.setSelectedIndex(0);
        }else {
            comboBox.setSelectedItem(download);
        }


    }



    public void upLoadFile() {
        if (!textField.getText().equals("") && textField.getText() !=null) {

            // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20M以下的文件使用该接口
            // 大文件上传请参照 API 文档高级 API 上传
            File localFile = new File(file.getAbsolutePath());
            // 指定要上传到 COS 上对象键
            // 对象键（Key）是对象在存储桶中的唯一标识。例如，在对象的访问域名 `bucket1-1250000000.cos.ap-guangzhou.myqcloud.com/doc1/pic1.jpg` 中，对象键为 doc1/pic1.jpg, 详情参考 [对象键](https://cloud.tencent.com/document/product/436/13324)
            String key = jfc.getSelectedFile().getName();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);

        } else {
            System.out.println("不能为空");
        }
    }




    public void downLoad(){
        String key = (String) comboBox.getSelectedItem();
        File downFile = new File(courseFile+"/"+"downloads"+"/"+key);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        ObjectMetadata downObjectMeta = cosclient.getObject(getObjectRequest, downFile);
        cosclient.shutdown();
    }



/*        String key = "gServer.jar";
        // 指定要下载到的本地路径
        File downFile = new File("src/main/test/gServer.jar");
        // 指定要下载的文件所在的 bucket 和对象键
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        ObjectMetadata downObjectMeta = cosclient.getObject(getObjectRequest, downFile);*/




}

