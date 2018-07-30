import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.util.*;

public class ForBlogCDN {

    public static void main(String[] args) {



        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("一分钟一次检测开始。。。。");
                ForBlogCDN cdn = new ForBlogCDN();
                cdn.init();
                cdn.upload(cdn.getNotHave());

                System.out.println("/n/n");
                System.out.println(new Date());
                System.out.println("检测结束。。。");
            }

        },10000,20000);




    }






    // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
    String bucketName = "blog-1251143468";
    //客户端
    COSClient cosclient = null;


    public void  init(){
        COSCredentials cred = new BasicCOSCredentials("AKIDX3R2uf33h9RXZm63BLfw9SefVEmALPF3",
                "nX8YckPtCKH2d3IXcWItJmBPSJzUnwui");
        //地域的简称参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-shanghai"));
        // 生成cos客户端
        cosclient = new COSClient(cred, clientConfig);
    }

    //服务器文件列表
    public List<String> getYunList(){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        String prefix = getPrefix();
        listObjectsRequest.setPrefix(prefix);
        listObjectsRequest.setMaxKeys(999);
        ObjectListing objectListing = cosclient.listObjects(listObjectsRequest);
        List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        List<String> bucketList = new ArrayList<String>();
        for (COSObjectSummary cosObjectSummary : objectSummaries) {
            // 腾讯云文件列表
            String key = cosObjectSummary.getKey().replace(prefix,"");
            if (!key.equals("")) {
                bucketList.add(key);
            }
        }
        return bucketList;
    }


    //取得不包含的文件列表
    public List<String> getNotHave(){
        File file = new File("C:/Users/WitQue/Desktop/"+getPrefix());
        List<String> bucketList = getYunList();
        List<String> localList = Arrays.asList(file.list());
        List<String> notHaveList = new ArrayList<String>();

        for (String local : localList){
            boolean flag = bucketList.contains(local);
            if (!flag){
                notHaveList.add(local);
            }
        }

        return notHaveList;
    }


    public void upload(List<String> list){


        for (String upName:list) {
            //指定本地文件路径
            File localFile = new File("C:/Users/WitQue/Desktop/"+getPrefix()+ upName);

            //指定对象键
            String key = "test/"+ upName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);

            System.out.println(key+"/t"+"success");
        }

    }



















    //前缀
    private static String getPrefix(){
        Calendar calendar =Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        if (month.length()<2){
            month = "0"+month;
        }
        String prefix = year + "/" +month+"/";
        return prefix;
    }



}
