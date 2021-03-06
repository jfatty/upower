package jason.app.weixin.web.controller.rest;

import jason.app.weixin.common.constant.MediaType;
import jason.app.weixin.common.model.FileInfo;
import jason.app.weixin.common.model.FileItem;
import jason.app.weixin.common.service.IAmazonS3Service;
import jason.app.weixin.common.service.IFileService;
import jason.app.weixin.security.model.User;
import jason.app.weixin.security.service.ISecurityService;
import jason.app.weixin.web.controller.rest.model.Image;
import jason.app.weixin.web.controller.rest.model.UploadResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/rest")
public class FileUploadController {
	
	private static Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	
	@Autowired
	private IFileService fileService;
	
	@Autowired
	private IAmazonS3Service s3Service;
	
	@Autowired
	private ISecurityService securityService;
	
	private Calendar calendar = Calendar.getInstance();

	@RequestMapping(value = "/upload")
	@Transactional
    public @ResponseBody UploadResult handleFormUpload(HttpServletRequest req,HttpServletResponse resp,@RequestParam("file") MultipartFile file){
		User user = securityService.getCurrentUser();
		UploadResult result = new UploadResult();
		try {
			File tempFile = File.createTempFile("weaktie", "tmp");
			file.transferTo(tempFile);
			FileInfo fileInfo = new FileInfo();
			fileInfo.setContentType(file.getContentType());
			fileInfo.setFileName(file.getName());
			fileInfo.setMediaType(MediaType.IMAGE);
			fileInfo.setFile(tempFile);
			
			fileInfo = fileService.resizePhoto(fileInfo);
			
			FileInfo thumbnail = fileService.createThumbnail(fileInfo);
			String mediaUrl = s3Service.saveFile(fileInfo);
			logger.info("media url "+mediaUrl);
			String thumbnailUrl =null;
			if(thumbnail!=null) {
				thumbnailUrl = s3Service.saveFile(thumbnail);
			}
			logger.info("thumbnail url "+mediaUrl);
			FileItem fileItem = new FileItem();
			fileItem.setUrl(mediaUrl);
			fileItem.setThumbnail(thumbnailUrl);
			fileItem.setCreateDate(new Date());
			fileItem.setUserId(user.getId());
			fileItem.setContentType(fileInfo.getContentType());
			fileItem.setMediaType(MediaType.IMAGE);
			fileItem.setFileName(file.getOriginalFilename());
			fileItem.setSize(file.getSize());
			fileService.saveFile(fileItem);
			result.setFilelink(mediaUrl);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		return result;
    }
	
	@RequestMapping(value = "/uploadFile")
	@Transactional
    public @ResponseBody jason.app.weixin.web.controller.rest.model.File uploadFile(HttpServletRequest req,HttpServletResponse resp,@RequestParam("file") MultipartFile file){
		User user = securityService.getCurrentUser();
		jason.app.weixin.web.controller.rest.model.File result = new jason.app.weixin.web.controller.rest.model.File();
		try {
			File tempFile = File.createTempFile("weaktie", "tmp");
			file.transferTo(tempFile);
			FileInfo fileInfo = new FileInfo();
			fileInfo.setContentType(file.getContentType());
			fileInfo.setFileName(file.getName());
			fileInfo.setMediaType(MediaType.FILE);
			fileInfo.setFile(tempFile);

			String mediaUrl = s3Service.saveFile(fileInfo);
			
			logger.info("thumbnail url "+mediaUrl);
			FileItem fileItem = new FileItem();
			fileItem.setUrl(mediaUrl);
			
			fileItem.setCreateDate(new Date());
			fileItem.setUserId(user.getId());
			fileItem.setContentType(fileInfo.getContentType());
			fileItem.setMediaType(MediaType.FILE);
			fileItem.setFileName(file.getOriginalFilename());
			fileItem.setSize(file.getSize());
			fileService.saveFile(fileItem);
			result.setLink(mediaUrl);
			result.setName(fileItem.getFileName());
			result.setSize(decodeSize(fileItem.getSize()));
			result.setTitle(fileItem.getFileName());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		return result;
    }

	@RequestMapping(value = "/images")
    public @ResponseBody List<Image> images(HttpServletRequest req,HttpServletResponse resp){
		List<Image> images = new ArrayList<Image>();
		User user = securityService.getCurrentUser();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		Date lastDay = calendar.getTime();
		List<FileItem> files = fileService.findImagesByUser(user.getId(),lastDay,new Date());
		
		for(FileItem file:files) {
			Image image = new Image();
			image.setImage(file.getUrl());
			image.setThumb(file.getThumbnail());
			image.setFilelink(file.getUrl());
			image.setFolder("过去一天");
			images.add(image);
		}
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		Date lastWeek = calendar.getTime();
		files = fileService.findImagesByUser(user.getId(),lastWeek,lastDay);
		
		for(FileItem file:files) {
			Image image = new Image();
			image.setImage(file.getUrl());
			image.setThumb(file.getThumbnail());
			image.setFilelink(file.getUrl());
			image.setFolder("过去一周");
			images.add(image);
		}
		
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -1);
		Date lastMonth = calendar.getTime();
		files = fileService.findImagesByUser(user.getId(),lastMonth,lastWeek);
		
		for(FileItem file:files) {
			Image image = new Image();
			image.setImage(file.getUrl());
			image.setThumb(file.getThumbnail());
			image.setFilelink(file.getUrl());
			image.setFolder("过去一月");
			images.add(image);
		}
		
		
		return images;
    }
	
	
	@RequestMapping(value = "/files")
    public @ResponseBody List<jason.app.weixin.web.controller.rest.model.File> files(HttpServletRequest req,HttpServletResponse resp){
		List<jason.app.weixin.web.controller.rest.model.File> images = new ArrayList<jason.app.weixin.web.controller.rest.model.File>();
		User user = securityService.getCurrentUser();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -1);
		Date lastDay = calendar.getTime();
		List<FileItem> files = fileService.findFilesByUser(user.getId(),lastDay,new Date());
		
		for(FileItem file:files) {
			jason.app.weixin.web.controller.rest.model.File image = new jason.app.weixin.web.controller.rest.model.File();
			image.setTitle(file.getFileName());
			image.setName(file.getFileName());
			image.setSize(decodeSize(file.getSize()));
			image.setLink(file.getUrl());
			images.add(image);
		}
		
		return images;
	}
	
	@RequestMapping(value = "/videos")
    public @ResponseBody List<Image> videos(HttpServletRequest req,HttpServletResponse resp){
		List<Image> images = new ArrayList<Image>();
		User user = securityService.getCurrentUser();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -1);
		Date lastDay = calendar.getTime();
		List<FileItem> files = fileService.findVideosByUser(user.getId(),lastDay,new Date());
		
		for(FileItem file:files) {
			Image image = new Image();
			image.setImage(file.getUrl());
			image.setThumb(file.getThumbnail());
			image.setFilelink(file.getUrl());
			image.setFolder("过去一月");
			images.add(image);
		}
		
		return images;
	}

	private String decodeSize(Long size) {
		// TODO Auto-generated method stub
		if(size==null) return null;
		String [] surfix = {"B","KB","MB","GB"};
		Long sz = size;
		int pos = 0;
		while(sz>1024) {
			sz = sz/1024;
			pos++;
		}
		return sz+surfix[pos];
	}

}
