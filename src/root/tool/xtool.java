package root.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class xtool {
	
	/** 可持续更新这个工具类,不被每次更新弄乱,更新便注入时间,导出固定文件名,只导源代码不编译 **/
	/** 2019年7月11日 14:14:06 **/
	/** 2019年7月30日 17:07:31 **/
	/** 2019年8月1日 23:53:54 **/
	/** 2019年8月5日 15:39:58 **/
	/** 2019年8月23日 12:21:55 **/
	/** 2019年9月1日 16:28:44 **/
	
	public static StringBuilder x字符转码(String xname) {
		StringBuilder x_StringBuilder = new StringBuilder();
		// 仅转中文
		char[] x_char = xname.toCharArray();
		for (char x_charL : x_char) {
			boolean x_is_chinese = String.valueOf(x_charL).matches("[\u4e00-\u9fa5]");
			if (x_is_chinese) {
				try {
					x_StringBuilder.append(URLEncoder.encode(String.valueOf(x_charL), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				x_StringBuilder.append(x_charL);
			}
			
		}
		
		return x_StringBuilder;
	}
	
	public static void x请求信息(HttpServletRequest request) {
		StringBuilder xinfo = new StringBuilder();
		
		xinfo.append("Attribute:\n");
		Enumeration<String> xAttribute = request.getAttributeNames();
		while (xAttribute.hasMoreElements()) {
			String xAttributeL = (String) xAttribute.nextElement();
			String xValues = (String) request.getAttribute(xAttributeL);
			xinfo.append(xAttributeL + ":" + xValues + "\n");
		}
		
		xinfo.append("Parameter: \n");
		Enumeration<String> xParameter = request.getParameterNames();
		while (xParameter.hasMoreElements()) {
			String xParameterL = (String) xParameter.nextElement();
			String xValues = request.getParameter(xParameterL);
			xinfo.append(xParameterL + ":" + xValues + "\n");
		}
		
		xinfo.append("Header:\n");
		Enumeration<String> xHeader = request.getHeaderNames();
		while (xHeader.hasMoreElements()) {
			String xHeaderL = xHeader.nextElement();
			String xValues = request.getHeader(xHeaderL);
			xinfo.append(xHeaderL + ":" + xValues + "\n");
		}
		
		System.out.println("请求信息:\n" + xinfo.toString());
	}
	
	public static void x响应信息(HttpServletResponse response) {
		StringBuilder xinfo = new StringBuilder();
		
		xinfo.append("Header:\n");
		Collection<String> xHeader = response.getHeaderNames();
		for (String xHeaderL : xHeader) {
			String xValues = response.getHeader(xHeaderL);
			Collection<String> xHeaders = response.getHeaders(xValues);
			for (String xHeadersL : xHeaders) {
				xinfo.append("Headers:" + xHeadersL + "\n");
			}
			xinfo.append(xHeaderL + ":" + xValues + "\n");
		}
		
//		xinfo.append("TrailerFieids:\n");
//		Supplier<Map<String, String>> xTrailerFields = response.getTrailerFields();
//		if (xTrailerFields.get() != null) {
//			xinfo.append("TrailerFieids:" + xTrailerFields.toString());
//			Map<String, String> xTrailerFieldsL = xTrailerFields.get();
//			Set<String> xkeySet = xTrailerFieldsL.keySet();
//			Iterator<String> xkeySetIterator = xkeySet.iterator();
//			while (xkeySetIterator.hasNext()) {
//				String xkeyL = (String) xkeySetIterator.next();
//				String xValues = xTrailerFieldsL.get(xkeyL);
//				xinfo.append(xkeyL + ":" + xValues + "\n");
//			}
//		}
		
		xinfo.append("BufferSize:" + response.getBufferSize() + "\n");
		xinfo.append("CharacterEncoding:" + response.getCharacterEncoding() + "\n");
		xinfo.append("ContentType:" + response.getContentType() + "\n");
		xinfo.append("Status:" + response.getStatus() + "\n");
		xinfo.append("Locale:" + response.getLocale() + "\n");
		
		System.out.println("响应信息:\n" + xinfo.toString());
	}
	
	/**
	 * 
	 * 简化处理,读取文本.
	 * 默认为 HTML!
	 * @param 将读取该文件
	 * @return HTML 文件的 byte[] 形式
	 */
	public static byte[] x读取文本(File x路径) {
		
		byte[] xbr = null;
		if (x路径 == null || !x路径.exists()) {
			// 返回一个错误提示网页
			try {
				return ("<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
				                + "	<meta charset=\"UTF-8\">\r\n"
				                + "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
				                + "	<meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n"
				                + "	<title>-----未发现文件-----</title>\r\n" + "</head>\r\n"
				                + "<body>\r\n" +
				                
				                "	<a>该文件不存在: " + x路径 + "</a>\r\n" +
				                
				                "</body>\r\n" + "</html>").getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
			}
			
		} else {
			try {
				FileInputStream xi = new FileInputStream(x路径);
				
				xbr = new byte[xi.available()];
				xi.read(xbr);
				
				xi.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return xbr;
		}
		return null;
	}
	
	public static void x请求头(HttpServletRequest request) {
		
		System.out.println("请求头:");
		Enumeration<String> xH = request.getParameterNames();
		System.out.println("Parameter:");
		while (xH.hasMoreElements()) {
			String xHeaderL = xH.nextElement();
			String xValues = request.getParameter(xHeaderL);
			System.out.println(xHeaderL + ":" + xValues);
		}
	}
	
	public static long[] xRange_处理(String xRange) {
		long xbeginindex = 0;
		long xendindex = 0;
		int x干号 = xRange.indexOf("-");
		int x等号 = xRange.indexOf("=");
		int xLength = xRange.length();
		
		// bytes=-256 判断=后面
		// 是 0
		// 不是 xxxx
		if (xRange.subSequence(x等号 + 1, x干号 + 1).equals("-")) {
			xbeginindex = 0;
		} else {
			String xbegin = xRange.substring(x等号 + 1, x干号);
			xbeginindex = Long.parseLong(xbegin);
		}
		
		// bytes=xxxx-[nnnn] 最后一个字符是否是-
		// 是 xxxx+1024*8
		// 不是 nnnn
		if (xRange.substring(xLength - 1, xLength).equals("-")) {
			xendindex = xbeginindex + 1024 * 8;
		} else {
			String xend = xRange.substring(x干号 + 1, xLength);
			xendindex = Long.parseLong(xend);
		}
		return new long[] { xbeginindex, xendindex };
	}
	
	public void x输出文件MimeType() {
		StringBuilder xcode = new StringBuilder();
		xcode.append("将这段代码复制到 doget 里面!/n");
		xcode.append("String[] xFileType =new String[] \n" + "				{\n"
		                + "				 \"这里需要修改!"
		                + "				 \"src/《人与自然》 狂野非洲—山脉（下）.lnk \n"
		                + "				 \"src/青青子衿copy.pdf\n"
		                + "				};\r\n" + "		System.out.println(\"获取文件类型:\n"
		                + "		for (String xType : xFileType) {\n"
		                + "			String xTypeValues = getServletContext().getMimeType(xType);\n"
		                + "			System.out.println(xType+\"    \"+xTypeValues);\n"
		                + "		}");
		System.out.println(xcode);
	}
	
	/**
	 * @param 响应对象 待修 2019年8月5日 15:39:24
	 * @return 内置404网页如果不存在,本方法生成404网页
	 */
	public static IOException x404_回复(HttpServletResponse response) {
		IOException exception = null;
		response.setStatus(404);
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		byte[] xbyte_2 = xtool.x读取文本(new File(
		                "C:\\Users\\lenovo\\Desktop\\AppProjects\\项目 05-28_搬移\\ROOT3\\WebRoot\\not.html"));
		response.addHeader("Content-Length", xbyte_2.length + "");
		try {
			response.getOutputStream().write(xbyte_2);
			response.flushBuffer();
		} catch (IOException e) {
			exception = e;
		}
		if (exception != null) {
			return exception;
		}
		return exception;
	}
	
	
	
	
	
	/**
	 * 基类:byte to response
	 * 将字节信息进行传输
	 * @param xbyte 字节信息
	 * @param response 响应对象
	 * @throws Exception 抛出可能出现的异常
	 */
	public static void x_HTML_to_Byte_to_Response(byte[] xbyte,HttpServletResponse response) throws Exception {
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			response.addHeader("Content-Length", xbyte.length + "");
			response.getOutputStream().write(xbyte);
			response.flushBuffer();
		} catch (IOException e) {
			throw new Exception(" xtool工具内抛出异常:" + e.fillInStackTrace());
		}
	}
	
	/**
	 * 在基类的基础上简化的方法
	 * 基于基类的简化处理方法
	 * 将传入字符以 utf-8 进行编码
	 * @param string 字符串对象
	 * @param response 响应对象
	 * @throws Exception  抛出可能出现的异常
	 */
	public static void x_HTML_to_Byte_to_Response(String string,HttpServletResponse response) throws Exception {
		/*
		 * getbytes 会根据系统的编码为默认编码
		 * */
		x_HTML_to_Byte_to_Response(string.getBytes("UTF-8"), response);
	}
	
	/**
	 * 在基类的基础上简化的方法
	 * 基于基类的简化处理方法
	 * @param stringbuilder 可变的字符构造器
	 * @param response 响应对象
	 * @throws Exception 抛出可能出现的异常
	 */
	public static void x_HTML_to_Byte_to_Response(StringBuilder stringbuilder,HttpServletResponse response) throws Exception {
		x_HTML_to_Byte_to_Response(stringbuilder.toString(), response);
	}
	
	/**
	 * 在基类的基础上简化的方法
	 * 响应的参数已经默认设置,不适应的情况下请自行修改
	 * 
	 * @param file     读取HTML文件,进行响应传输
	 * @param response 响应对象
	 * @throws Exception 抛出可能出现的异常
	 */
	public static void x_HTML_to_Byte_to_Response(File file, HttpServletResponse response) throws Exception {
		byte[] xbyte=x读取文本(file);
		x_HTML_to_Byte_to_Response(xbyte, response);
	}
	
	

	/**
	 * 扫描当前项目所有注册的servlet 入口,将结果处理成网页!
	 * (主要是方便在发布项目时,方便的测试各个 servlet)
	 * @param 传入这个参数只是为了得到 getServletContext()
	 * @return 将结果拼接成网页,进行返回
	 */
	public static StringBuilder x_servlet_urlPatterns_扫描_to_HTML(HttpServletRequest request) {
		StringBuilder x_StringBuffer = new StringBuilder();

		// 获取当前项目登记信息集合
		Map<String, ? extends ServletRegistration> x_servlet登记信息 = request.getServletContext().getServletRegistrations();
		Iterator<String> x_keyset_iterator = x_servlet登记信息.keySet().iterator();
		while (x_keyset_iterator.hasNext()) {
			String x_keysetL = (String) x_keyset_iterator.next();
			
			if (x_keysetL.equals("default") || x_keysetL.equals("jsp")) {
				// 不扫描默认servlet
				continue;
			}
			
			ServletRegistration x_独个_servlet_登记信息 = x_servlet登记信息.get(x_keysetL);
			Collection<String> x_Mappings = x_独个_servlet_登记信息.getMappings();
			for (String x_urlPatterns : x_Mappings) {
				x_StringBuffer
				.append("<a href=\".")
				.append(x_urlPatterns )
				.append("\">")
				.append( x_urlPatterns )
				.append(x_独个_servlet_登记信息.getName())
				.append("   :  ")
				.append("</a><br><br>");
			}

		}
		return x_StringBuffer;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(new String(x读取文本(null), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
