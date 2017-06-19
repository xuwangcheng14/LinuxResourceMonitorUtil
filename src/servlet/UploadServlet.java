package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import util.consts.LinuxConstant;



public class UploadServlet extends HttpServlet {
	
    private static final long serialVersionUID = 1L;
      
    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "uploadDir";
  
    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 50;  // 50MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 1024; // 1G
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 1024; //1G
  
    /**
     * 上传数据及保存文件
     */
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
    	
    	request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
    	
    	PrintWriter out = response.getWriter();
    	
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("returnCode", LinuxConstant.ERROR_RETURN_CODE);
    	data.put("msg", "");
    	
        // 检测是否为多媒体上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            // 如果不是则停止
        	data.put("msg", "Error: 表单必须包含 enctype=multipart/form-data");
        	out.println(JSONObject.fromObject(data));    
            return;
        }
  
        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // 设置临时存储目录
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
  
        ServletFileUpload upload = new ServletFileUpload(factory);
          
        // 设置最大文件上传值
        upload.setFileSizeMax(MAX_FILE_SIZE);
          
        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(MAX_REQUEST_SIZE);
  
        // 构造临时路径来存储上传的文件
        // 这个路径相对当前应用的目录
        String uploadPath = getServletContext().getRealPath("") + File.separator;
        
          
        // 如果目录不存在则创建
        File uploadDir = new File(uploadPath + UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
  
        try {
            // 解析请求的内容提取文件数据
            List<FileItem> formItems = upload.parseRequest(request);
  
            if (formItems != null && formItems.size() > 0) {
                // 迭代表单数据
                for (FileItem item : formItems) {
                    // 处理不在表单中的字段
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        String filePath = UPLOAD_DIRECTORY + File.separator + fileName;
                        String savePath = uploadPath + filePath;
                        File storeFile = new File(savePath);
                        // 在控制台输出文件的上传路径
                        System.out.println(savePath);
                        System.out.println(this.getServletContext().getContextPath() + "/" + UPLOAD_DIRECTORY + "/" + fileName);
                        // 保存文件到硬盘
                        item.write(storeFile);
                        data.put("returnCode", LinuxConstant.CORRECT_RETURN_CODE);
                    	data.put("msg", "文件上传成功!");
                    	data.put("filePath", this.getServletContext().getContextPath() + "/" + UPLOAD_DIRECTORY + "/" + fileName);
                    }
                }
            }
        } catch (Exception ex) {
        	data.put("msg", ex.getMessage());
        }
        // 跳转到 message.jsp
        out.println(JSONObject.fromObject(data));
    }
}
