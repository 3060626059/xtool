package root.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import root.tool.xtool;

/**
 * @author 一个处理文件流传输的工具类,这个Java文件包含多个类,
 * 需要传入 请求(非必要),响应(必要),文件路径
 * 
 * 下载_传输工具,这个类需要引用这个包的另一个类,复制时要修改.
 * 
 * 复用
 */
public class dowloadS {
	
	public dowloadS() {
		super();
	}
	
	/**
	 * @param 请求封装对象
	 * @param 响应封装对象response
	 * @param 待传输文件
	 * @throws 文件不存在异常 Exception
	 */
	public static void x_传输流(HttpServletRequest request, HttpServletResponse response,File x_文件) throws Exception {

		if (x_文件.exists()==false) {
			//  测试抛出异常
			throw new FileNotFoundException();
		}
		
		int x_status = 0;
		// String x_file_name = null;
		String x_MimeType = null;
		String x_ContentType = null;
		Long x_file_Length = 0L;
		
		// x_file_name = xtool.x字符转码(x_文件.getName()).toString();
		x_MimeType = request.getServletContext().getMimeType(x_文件.toString());
		x_ContentType = x_MimeType;
		x_file_Length = x_文件.length();
				
		response.setHeader("Accept-Ranges", "bytes");
		response.setCharacterEncoding("UTF-8");
		boolean x_is_Range = request.getHeader("Range") == null;
		if (x_is_Range) {
			// 非断点
			// 响应码>>类型>>文件下载弹窗>>文件长度>>传输流
			x_status = 200;
			
			response.setStatus(x_status);
			response.setContentType(x_ContentType);
			// response.setHeader("Content-Disposition", "attachment; filename=\"" + x_file_name + "\"");
			response.setContentLengthLong(x_file_Length);
			FileInputStream xInputStream = null;
			try {
				xInputStream = new FileInputStream(x_文件);
				copy(xInputStream, response.getOutputStream());
				xInputStream.close();
			} catch (IOException e) {
			}
		} else {
			// 断点
			// 响应码>>类型>>文件下载弹窗>>断点范围 信息>>文件长度>>传输流
			x_status = 206;
			response.setStatus(x_status);
			response.setContentType(x_ContentType);
			// response.setHeader("Content-Disposition", "attachment; filename=" + x_file_name);
			ArrayList<Range> x_Range = new ArrayList<Range>();
			FileInputStream xInputStream = null;
			try {
				x_Range = parseRange(request, response, x_文件);
				Range range = x_Range.get(0);
				response.addHeader("Content-Range",
				                "bytes " + range.start + "-" + range.end + "/" + range.length);
				long length = range.end - range.start + 1;
				response.setContentLengthLong(length);
				xInputStream = new FileInputStream(x_文件);
				copy(xInputStream, response.getOutputStream(), range);
				xInputStream.close();
			} catch (IOException e) {
			}
		}
		
	}
	
	public static void x_下载_传输流(HttpServletRequest request, HttpServletResponse response,File x_文件) {

		if (x_文件.exists()==false) {
			return;
		}
		
		int x_status = 0;
		String x_file_name = null;
		String x_MimeType = null;
		String x_ContentType = null;
		Long x_file_Length = 0L;
		
		x_file_name = xtool.x字符转码(x_文件.getName()).toString();
		x_MimeType = request.getServletContext().getMimeType(x_文件.toString());
		x_ContentType = x_MimeType;
		x_file_Length = x_文件.length();
		
		response.setHeader("Accept-Ranges", "bytes");
		response.setCharacterEncoding("UTF-8");
		boolean x_is_Range = request.getHeader("Range") == null;
		if (x_is_Range) {
			// 非断点
			// 响应码>>类型>>文件下载弹窗>>文件长度>>传输流
			x_status = 200;
			
			response.setStatus(x_status);
			response.setContentType(x_ContentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + x_file_name + "\"");
			response.setContentLengthLong(x_file_Length);
			FileInputStream xInputStream = null;
			try {
				xInputStream = new FileInputStream(x_文件);
				copy(xInputStream, response.getOutputStream());
				xInputStream.close();
			} catch (IOException e) {
			}
		} else {
			// 断点
			// 响应码>>类型>>文件下载弹窗>>断点范围 信息>>文件长度>>传输流
			x_status = 206;
			response.setStatus(x_status);
			response.setContentType(x_ContentType);
			response.setHeader("Content-Disposition", "attachment; filename=" + x_file_name);
			ArrayList<Range> x_Range = new ArrayList<Range>();
			FileInputStream xInputStream = null;
			try {
				x_Range = parseRange(request, response, x_文件);
				Range range = x_Range.get(0);
				response.addHeader("Content-Range",
				                "bytes " + range.start + "-" + range.end + "/" + range.length);
				long length = range.end - range.start + 1;
				response.setContentLengthLong(length);
				xInputStream = new FileInputStream(x_文件);
				copy(xInputStream, response.getOutputStream(), range);
				xInputStream.close();
			} catch (IOException e) {
			}
		}
	}
	
	/** 复制过来的代码>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> **/
	
	/**
	 * Parse the range header. 解析范围标题。 需要进行测试,它是怎么工作的? 使用方法,直接使用FULL
	 *
	 * @param request  The servlet request we are processing
	 * @param response The servlet response we are creating
	 * @param resource The resource
	 * @return a list of ranges, {@code null} if the range header was invalid or
	 *         {@code #FULL} if the Range header should be ignored. 范围列表,
	 *         {@code null} 如果范围标头无效或
	 *         {@code #FULL/protected static final ArrayList<Range> FULL = new ArrayList<>();}
	 *         如果应该忽略范围标题。
	 * @throws IOException an IO error occurred
	 */
	private static ArrayList<Range> parseRange(HttpServletRequest request, HttpServletResponse response, File resource)
	                throws IOException {
		
		// 在原来代码的类中 FULL 这个数组是 静态创建,即在这个生命周期中不会发生改变
		// 复制代码需要这个对象,不能在类静态创建,所以由这个方法创建
		// 修改一处
		final ArrayList<Range> FULL = new ArrayList<>();
		
		// Range headers are only valid on GET requests. That implies they are
		// also valid on HEAD requests. This method is only called by doGet()
		// and doHead() so no need to check the request method.
		
		// Checking If-Range
		// 范围头只对GET请求有效。
		// 这意味着它们对于HEAD请求也是有效的。
		// 此方法仅由doGet()和doHead()调用，因此不需要检查请求方法。
		
		// 检查If-Range
		String headerValue = request.getHeader("If-Range");
		
		if (headerValue != null) {
			
			long headerValueTime = (-1L);
			try {
				headerValueTime = request.getDateHeader("If-Range");
			} catch (IllegalArgumentException e) {
				// Ignore
				// 忽略
			}
			
			String eTag = headerValue;// resource.getETag();
			long lastModified = -1L;
			
			if (headerValueTime == (-1L)) {
				// If the ETag the client gave does not match the entity
				// etag, then the entire entity is returned.
				// 如果客户端提供的ETag与实体ETag不匹配,
				// 则返回整个实体。
				if (!eTag.equals(headerValue.trim())) {
					// FULL 是已经定义好的数组
					return FULL;
				}
			} else {
				// If the timestamp of the entity the client got differs from
				// the last modification date of the entity, the entire entity
				// is returned.
				// 如果客户端获得的实体的时间戳与实体的最后修改日期不同，
				// 则返回整个实体。
				if (Math.abs(lastModified - headerValueTime) > 1000) {
					return FULL;
				}
			}
		}
		
		long fileLength = resource.length();
		
		if (fileLength == 0) {
			// Range header makes no sense for a zero length resource. Tomcat
			// therefore opts to ignore it.
			// 范围头对于零长度的资源没有任何意义。
			// 因此tomcatso选择忽略它。
			// 请求的资源长度是0,说明该文件没有内容,无法返回有效内容,
			// 选择丢弃处理,
			return FULL;
		}
		
		// Retrieving the range header (if any is specified
		// 检索范围标头(如果指定了任何标头)
		// 这里到1648行是从请求获取Range参数,进行逻辑处理!
		String rangeHeader = request.getHeader("Range");
		
		if (rangeHeader == null) {
			// No Range header is the same as ignoring any Range header
			// 没有任何范围标头与忽略任何范围标头是相同的
			// 这里执行返回 ArrayList<Range> FULL = new ArrayList<>()
			// 意味着后面的代码不会执行
			// 请求中的Range参数,有两种情况,那么没有Range的情况下,
			// 是怎么决定传输文件的?
			return FULL;
		}
		// 创建一个新的字符串读取器。StringReader是一个java 基础类,不知道这个
		// 功能是什么
		// HTTP 头部封装
		Ranges ranges = Ranges.parse(new StringReader(rangeHeader));
		
		if (ranges == null) {
			// 更前的判断已经得到Range,经过封装处理返回null,是格式不正确
			// The Range header is present but not formatted correctly.
			// Could argue for a 400 response but 416 is more specific.
			// There is also the option to ignore the (invalid) Range header.
			// RFC7233#4.4 notes that many servers do ignore the Range header in
			// these circumstances but Tomcat has always returned a 416.
			// 出现范围标头，但格式不正确。
			// 可以支持400个响应，但416更具体。
			// 还有一个选项可以忽略(无效的)范围头。
			// RFC7233#4.4指出，在这些情况下，
			// 许多服务器确实忽略了范围头，但是Tomcat总是返回416。
			response.addHeader("Content-Range", "bytes */" + fileLength);
			response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			return null;
		}
		
		// bytes is the only range unit supported (and I don't see the point
		// of adding new ones).
		// 字节是惟一受支持的范围单元(我不认为添加新的范围单元有什么意义)。
		// 说明范围请求不限于 Byte,Char也可以,猜测
		if (!ranges.getUnits().equals("bytes")) {
			// RFC7233#3.1 Servers must ignore range units they don't understand
			// RFC7233#3.1 服务器必须忽略它们不理解的范围单位
			return FULL;
		}
		
		// 对获取的 Range头部进行三处判断
		// 1.存在范围请求?
		// 2.是正确的格式?
		// 3.是 bytes(字节) 形式的范围请求?
		// 下面才是真正的处理
		
		// TODOS: Remove the internal representation and use Ranges
		// Convert to internal representation
		// 删除内部表示，并将使用范围转换为内部表示
		// 并将使用范围转换为内部表示
		// 从上面取得请求 Range信息 会传给Ranges处理封装成条目对象
		// 循环取得Ranges包装器中的条目,调用条目方法得到数字
		// 将得到数字赋予新建对象 Range(对数字进行判断,-1则进行自定义处理)
		// Range对象对值进行检查,不合格跳过后面的代码
		// 将Range对象添加到 数组<Range>
		// 这个方法最后将返回这个数组
		ArrayList<Range> result = new ArrayList<>();
		
		for (Ranges.Entry entry : ranges.getEntries()) {
			Range currentRange = new Range();
			if (entry.getStart() == -1) {
				currentRange.start = fileLength - entry.getEnd();
				if (currentRange.start < 0) {
					currentRange.start = 0;
				}
				currentRange.end = fileLength - 1;
			} else if (entry.getEnd() == -1) {
				currentRange.start = entry.getStart();
				currentRange.end = fileLength - 1;
			} else {
				currentRange.start = entry.getStart();
				currentRange.end = entry.getEnd();
			}
			currentRange.length = fileLength;
			
			if (!currentRange.validate()) {
				response.addHeader("Content-Range", "bytes */" + fileLength);
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return null;
			}
			
			result.add(currentRange);
		}
		
		return result;
	}
	
	/**
	 * Copy the contents of the specified input stream to the specified output
	 * stream, and ensure that both streams are closed before returning (even in the
	 * face of an exception). 将指定输入流的内容复制到指定输出流， 并确保在返回前关闭这两个流( 即使在出现异常时)。
	 * 
	 * @param resource The source resource
	 * @param ostream  The output stream to write to
	 * @param range    Range the client wanted to retrieve
	 * @exception IOException if an input/output error occurs
	 */
	private static void copy(InputStream resource, ServletOutputStream ostream, Range range) throws IOException {
		
		IOException exception = null;
		
		InputStream resourceInputStream = resource;
		InputStream istream = new BufferedInputStream(resourceInputStream, 2048);
		exception = copyRange(istream, ostream, range.start, range.end);
		
		// Clean up the input stream
		// 清理输入流
		istream.close();
		
		// Rethrow any exception that has occurred
		// 重新抛出已发生的任何异常
		if (exception != null)
			throw exception;
		
	}
	
	private static void copy(InputStream is, ServletOutputStream ostream) throws IOException {
		
		IOException exception = null;
		InputStream istream = new BufferedInputStream(is, 2048);
		
		// Copy the input stream to the output stream
		// 将输入流复制到输出流
		exception = copyRange(istream, ostream);
		
		// Clean up the input stream
		istream.close();
		
		// Rethrow any exception that has occurred
		// 重新抛出已发生的任何异常
		if (exception != null)
			throw exception;
	}
	
	private static IOException copyRange(InputStream istream, ServletOutputStream ostream, long start, long end) {
		
		long skipped = 0;
		try {
			skipped = istream.skip(start);
			// 跳过字节还有返回值?
			// 跳过字节成功的数量
		} catch (IOException e) {
			return e;
		}
		
		// 跳过字节数量少于应跳数量,抛异常
		if (skipped < start) {
			return new IOException("数据异常");
		}
		
		IOException exception = null;
		long bytesToRead = end - start + 1;// 应读取字节数量
		
		byte buffer[] = new byte[4096];
		int len = buffer.length;// 赋默认值
		
		// 应读取字节数量大于零 并且 len 大于或者等于 buffer.length ,条件同时成立
		// 当某次的输入流剩下的字节不足 2048 怎么办?
		// bytesToRead 表示要读字节的数量,这个每一次循环都递减
		// len 每次都会被重置,输入流将把buffer填满,并且把实际填字节的数量赋值给len
		// 进行判断 :
		// 应读取字节数量 大于当前 len ,则输出 buffer,字节起点 0,字节终点 len;
		// 若小于 len,则输出 buf应读取fer,字节起点 0,字节终点 应读取字节数量;
		// 在应读取字节数量 小于 len,说明完成上个方法交代的任务,buffer的数据有污染,不能全读
		// 每个判断内的都进行一次 应读字节数量的递减,减去当次读出数量
		// 最后进行异常判断,出异常会把len 重置为-1,让循环结束
		while ((bytesToRead > 0) && (len >= buffer.length)) {
			try {
				len = istream.read(buffer);// 读取的数量
				if (bytesToRead >= len) {
					ostream.write(buffer, 0, len);
					bytesToRead -= len;// bytesToRead=bytesToRead-len;
				} else {
					ostream.write(buffer, 0, (int) bytesToRead);
					bytesToRead = 0;
				}
			} catch (IOException e) {
				exception = e;
				len = -1;
			}
			if (len < buffer.length)
				break;
		}
		
		return exception;
		
	}
	
	private static IOException copyRange(InputStream istream, ServletOutputStream ostream) {
		
		// Copy the input stream to the output stream
		IOException exception = null;
		byte buffer[] = new byte[2048];
		int len = buffer.length;
		while (true) {
			try {
				len = istream.read(buffer);
				if (len == -1)
					break;
				ostream.write(buffer, 0, len);
			} catch (IOException e) {
				exception = e;
				len = -1;
				break;
			}
		}
		return exception;
		
	}
}


enum SkipResult {
    FOUND,
    NOT_FOUND,
    EOF
}




class Range {
	
	public long start;
	public long end;
	public long length;
	
	/**
	 * Validate range.
	 *验证范围。
	 * @return true if the range is valid, otherwise false
	 * 如果范围有效，则为真，否则为假
	 */
	public boolean validate() {
		if (end >= length)
			end = length - 1;
		return (start >= 0) && (end >= 0) && (start <= end) && (length > 0);
	}
}






class Ranges {

    private final String units;
    private final List<Entry> entries;


    private Ranges(String units, List<Entry> entries) {
        this.units = units;
        this.entries = Collections.unmodifiableList(entries);
    }


public static class Entry {

        private final long start;
        private final long end;


        public Entry(long start, long end) {
            this.start = start;
            this.end = end;
        }


        public long getStart() {
            return start;
        }


        public long getEnd() {
            return end;
        }
    }

    /**
     * Parses a Range header from an HTTP header. 从HTTP头解析范围头
     * 这个包装类中的枚举对象用法我不怎么明白?
     * 没有赋值能用来当参照?
     * 这个类核心功能是HttpParser这个实现的!
     * HttpParser这个包装类不关注!
     * 完全不理解从 HttpParser这个包装器取出数字的目的
     * 这个包装类内部计算的目的
     *
     * @param input a reader over the header text
     *
     * @return a set of ranges parsed from the input, or null if not valid 从输入解析的一组范围，如果无效则为null
     *
     * @throws IOException if there was a problem reading the input
     *
     **/
    public static Ranges parse(StringReader input) throws IOException {

        // Units (required) 单位(必需)
        String units = HttpParser.readToken(input);
        if (units == null || units.length() == 0) {
            return null;
        }

        // Must be followed by '=' 必须后跟'='
        if (HttpParser.skipConstant(input, "=") == SkipResult.NOT_FOUND) {
            return null;
        }

        // Range entries 范围条目
        List<Entry> entries = new ArrayList<>();

        // 这个是一个枚举类,我不知道它是在哪里被赋值的
        SkipResult skipResult;
        do {
            long start = HttpParser.readLong(input);
            // Must be followed by '-'
            // 必须后跟“-”
            if (HttpParser.skipConstant(input, "-") == SkipResult.NOT_FOUND) {
                return null;
            }
            long end = HttpParser.readLong(input);

            if (start == -1 && end == -1) {
                // Invalid range
        	// 无效的范围
                return null;
            }

            entries.add(new Entry(start, end));

            skipResult = HttpParser.skipConstant(input, ",");
            if (skipResult == SkipResult.NOT_FOUND) {
                // Invalid range 无效的范围
                return null;
            }
        } while (skipResult == SkipResult.FOUND);

        // There must be at least one entry
        // 必须至少有一个条目
        if (entries.size() == 0) {
            return null;
        }

        return new Ranges(units, entries);
    }
    
    public List<Entry> getEntries() {
        return entries;
    }

    public String getUnits() {
        return units;
    }

  
}



/** 需要的类复制过来<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< **/
/** 复制多个类中一个Java文件中,IDE只识别其中一个**/




/*
 * 使用说明:鉴于我目前的技术,我不能写出如此优秀
 * 可靠的代码,我以全复制进行学习,后期是否修改代码
 * 取决于应用环境.不是我亲手写的代码将保留原代码
 * 作者/组织 的痕迹(其中不包括违反国家法律,与不合适
 * 出现在项目中的注释,其他特殊情况).
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import org.apache.tomcat.util.res.StringManager;

/**
 * HTTP header value parser implementation. Parsing HTTP headers as per RFC2616
 * is not always as simple as it first appears. For headers that only use tokens
 * the simple approach will normally be sufficient. However, for the other
 * headers, while simple code meets 99.9% of cases, there are often some edge
 * cases that make things far more complicated.
 *
 * The purpose of this parser is to let the parser worry about the edge cases.
 * It provides tolerant (where safe to do so) parsing of HTTP header values
 * assuming that wrapped header lines have already been unwrapped. (The Tomcat
 * header processing code does the unwrapping.)
 * 
 * HTTP头值解析器实现。
 * 按照 RFC2616 解析HTTP报头并不总像它第一次出现时那么简单。
 * 对于只使用令牌的标头，简单的方法通常就足够了。
 * 然而，对于其他头文件，虽然简单的代码满足99.9%的情况，
 * 但经常有一些边缘情况使事情更加复杂。 
 * 
 * 这个解析器的目的是让解析器担心边缘情况。
 * 它提供了对HTTP头值的容忍(在安全的情况下)解析，
 * 假设包装的头行已经被解包。
 * (Tomcat头文件处理代码进行解包。)
 *
 */
 class HttpParser {

    private static final StringManager sm = StringManager.getManager(HttpParser.class);

    private static final int ARRAY_SIZE = 128;

    private static final boolean[] IS_CONTROL = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_SEPARATOR = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_TOKEN = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HEX = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HTTP_PROTOCOL = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_ALPHA = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_NUMERIC = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_UNRESERVED = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_SUBDELIM = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_USERINFO = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_RELAXABLE = new boolean[ARRAY_SIZE];

    private static final HttpParser DEFAULT;


    static {
        for (int i = 0; i < ARRAY_SIZE; i++) {
            // Control> 0-31, 127
            if (i < 32 || i == 127) {
                IS_CONTROL[i] = true;
            }

            // Separator 分隔符
            if (    i == '(' || i == ')' || i == '<' || i == '>'  || i == '@'  ||
                    i == ',' || i == ';' || i == ':' || i == '\\' || i == '\"' ||
                    i == '/' || i == '[' || i == ']' || i == '?'  || i == '='  ||
                    i == '{' || i == '}' || i == ' ' || i == '\t') {
                IS_SEPARATOR[i] = true;
            }

            // Token: Anything 0-127 that is not a control and not a separator
            // 令牌:任何0-127且不是控件和分隔符的内容
            if (!IS_CONTROL[i] && !IS_SEPARATOR[i] && i < 128) {
                IS_TOKEN[i] = true;
            }

            // Hex: 0-9, a-f, A-F
            if ((i >= '0' && i <='9') || (i >= 'a' && i <= 'f') || (i >= 'A' && i <= 'F')) {
                IS_HEX[i] = true;
            }

            // Not valid for HTTP protocol HTTP协议无效
            // "HTTP/" DIGIT "." DIGIT
            if (i == 'H' || i == 'T' || i == 'P' || i == '/' || i == '.' || (i >= '0' && i <= '9')) {
                IS_HTTP_PROTOCOL[i] = true;
            }

            if (i >= '0' && i <= '9') {
                IS_NUMERIC[i] = true;
            }

            if (i >= 'a' && i <= 'z' || i >= 'A' && i <= 'Z') {
                IS_ALPHA[i] = true;
            }

            if (IS_ALPHA[i] || IS_NUMERIC[i] || i == '-' || i == '.' || i == '_' || i == '~') {
                IS_UNRESERVED[i] = true;
            }

            if (i == '!' || i == '$' || i == '&' || i == '\'' || i == '(' || i == ')' || i == '*' ||
                    i == '+' || i == ',' || i == ';' || i == '=') {
                IS_SUBDELIM[i] = true;
            }

            // userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
            if (IS_UNRESERVED[i] || i == '%' || IS_SUBDELIM[i] || i == ':') {
                IS_USERINFO[i] = true;
            }

            // The characters that are normally not permitted for which the
            // restrictions may be relaxed when used in the path and/or query
            // string
            // 在路径 和/或 查询字符串中使用时，通常不允许放宽限制的字符
            if (i == '\"' || i == '<' || i == '>' || i == '[' || i == '\\' || i == ']' ||
                    i == '^' || i == '`'  || i == '{' || i == '|' || i == '}') {
                IS_RELAXABLE[i] = true;
            }
        }

        DEFAULT = new HttpParser(null, null);
    }


    private final boolean[] IS_NOT_REQUEST_TARGET = new boolean[ARRAY_SIZE];
    private final boolean[] IS_ABSOLUTEPATH_RELAXED = new boolean[ARRAY_SIZE];
    private final boolean[] IS_QUERY_RELAXED = new boolean[ARRAY_SIZE];


    public HttpParser(String relaxedPathChars, String relaxedQueryChars) {
        for (int i = 0; i < ARRAY_SIZE; i++) {
            // Not valid for request target.
            // Combination of multiple rules from RFC7230 and RFC 3986. Must be
            // ASCII, no controls plus a few additional characters excluded
            if (IS_CONTROL[i] ||
                    i == ' ' || i == '\"' || i == '#' || i == '<' || i == '>' || i == '\\' ||
                    i == '^' || i == '`'  || i == '{' || i == '|' || i == '}') {
                IS_NOT_REQUEST_TARGET[i] = true;
            }

            /*
             * absolute-path  = 1*( "/" segment )
             * segment        = *pchar
             * pchar          = unreserved / pct-encoded / sub-delims / ":" / "@"
             *
             * Note pchar allows everything userinfo allows plus "@"
             */
            if (IS_USERINFO[i] || i == '@' || i == '/') {
                IS_ABSOLUTEPATH_RELAXED[i] = true;
            }

            /*
             * query          = *( pchar / "/" / "?" )
             *
             * Note query allows everything absolute-path allows plus "?"
             */
            if (IS_ABSOLUTEPATH_RELAXED[i] || i == '?') {
                IS_QUERY_RELAXED[i] = true;
            }
        }

        relax(IS_ABSOLUTEPATH_RELAXED, relaxedPathChars);
        relax(IS_QUERY_RELAXED, relaxedQueryChars);
    }


    public boolean isNotRequestTargetRelaxed(int c) {
        // Fast for valid request target characters, slower for some incorrect
        // ones
        try {
            return IS_NOT_REQUEST_TARGET[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return true;
        }
    }


    public boolean isAbsolutePathRelaxed(int c) {
        // Fast for valid user info characters, slower for some incorrect
        // ones
        try {
            return IS_ABSOLUTEPATH_RELAXED[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public boolean isQueryRelaxed(int c) {
        // Fast for valid user info characters, slower for some incorrect
        // ones
        try {
            return IS_QUERY_RELAXED[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static String unquote(String input) {
        if (input == null || input.length() < 2) {
            return input;
        }

        int start;
        int end;

        // Skip surrounding quotes if there are any
        if (input.charAt(0) == '"') {
            start = 1;
            end = input.length() - 1;
        } else {
            start = 0;
            end = input.length();
        }

        StringBuilder result = new StringBuilder();
        for (int i = start ; i < end; i++) {
            char c = input.charAt(i);
            if (input.charAt(i) == '\\') {
                i++;
                result.append(input.charAt(i));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }


    public static boolean isToken(int c) {
        // Fast for correct values, slower for incorrect ones
        try {
            return IS_TOKEN[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isHex(int c) {
        // Fast for correct values, slower for some incorrect ones
        try {
            return IS_HEX[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isNotRequestTarget(int c) {
        return DEFAULT.isNotRequestTargetRelaxed(c);
    }


    public static boolean isHttpProtocol(int c) {
        // Fast for valid HTTP protocol characters, slower for some incorrect
        // ones
        try {
            return IS_HTTP_PROTOCOL[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isAlpha(int c) {
        // Fast for valid alpha characters, slower for some incorrect
        // ones
        try {
            return IS_ALPHA[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isNumeric(int c) {
        // Fast for valid numeric characters, slower for some incorrect
        // ones
        try {
            return IS_NUMERIC[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isUserInfo(int c) {
        // Fast for valid user info characters, slower for some incorrect
        // ones
        try {
            return IS_USERINFO[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    private static boolean isRelaxable(int c) {
        // Fast for valid user info characters, slower for some incorrect
        // ones
        try {
            return IS_RELAXABLE[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isAbsolutePath(int c) {
        return DEFAULT.isAbsolutePathRelaxed(c);
    }


    public static boolean isQuery(int c) {
        return DEFAULT.isQueryRelaxed(c);
    }


    // Skip any LWS and position to read the next character. The next character
    // is returned as being able to 'peek()' it allows a small optimisation in
    // some cases.
    static int skipLws(Reader input) throws IOException {

        input.mark(1);
        int c = input.read();

        while (c == 32 || c == 9 || c == 10 || c == 13) {
            input.mark(1);
            c = input.read();
        }

        input.reset();
        return c;
    }

    static SkipResult skipConstant(Reader input, String constant) throws IOException {
        int len = constant.length();

        skipLws(input);
        input.mark(len);
        int c = input.read();

        for (int i = 0; i < len; i++) {
            if (i == 0 && c == -1) {
                return SkipResult.EOF;
            }
            if (c != constant.charAt(i)) {
                input.reset();
                return SkipResult.NOT_FOUND;
            }
            if (i != (len - 1)) {
                c = input.read();
            }
        }
        return SkipResult.FOUND;
    }

    /**
     * @return  the token if one was found, the empty string if no data was
     *          available to read or <code>null</code> if data other than a
     *          token was found
     *          如果找到一个令牌，则返回空字符串(如果没有可用的数据);
     *          如果找到一个令牌以外的数据，则返回<code>null</code>
     *          
     */
    static String readToken(Reader input) throws IOException {
        StringBuilder result = new StringBuilder();

        skipLws(input);
        input.mark(1);
        int c = input.read();

        while (c != -1 && isToken(c)) {
            result.append((char) c);
            input.mark(1);
            c = input.read();
        }
        // Use mark(1)/reset() rather than skip(-1) since skip() is a NOP
        // once the end of the String has been reached.
        input.reset();

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     * @return  the digits if any were found, the empty string if no data was
     *          found or if data other than digits was found
     */
    static String readDigits(Reader input) throws IOException {
        StringBuilder result = new StringBuilder();

        skipLws(input);
        input.mark(1);
        int c = input.read();

        while (c != -1 && isNumeric(c)) {
            result.append((char) c);
            input.mark(1);
            c = input.read();
        }
        // Use mark(1)/reset() rather than skip(-1) since skip() is a NOP
        // once the end of the String has been reached.
        // 使用mark(1)/reset()而不是skip(-1)，
        // 因为一旦到达字符串的末尾，skip()就是NOP。
        input.reset();

        return result.toString();
    }

    /**
     * @return  the number if digits were found, -1 if no data was found
     *          or if data other than digits was found
     */
    static long readLong(Reader input) throws IOException {
        String digits = readDigits(input);

        if (digits.length() == 0) {
            return -1;
        }

        return Long.parseLong(digits);
    }

    /**
     * @return the quoted string if one was found, null if data other than a
     *         quoted string was found or null if the end of data was reached
     *         before the quoted string was terminated
     */
    static String readQuotedString(Reader input, boolean returnQuoted) throws IOException {

        skipLws(input);
        int c = input.read();

        if (c != '"') {
            return null;
        }

        StringBuilder result = new StringBuilder();
        if (returnQuoted) {
            result.append('\"');
        }
        c = input.read();

        while (c != '"') {
            if (c == -1) {
                return null;
            } else if (c == '\\') {
                c = input.read();
                if (returnQuoted) {
                    result.append('\\');
                }
                result.append((char) c);
            } else {
                result.append((char) c);
            }
            c = input.read();
        }
        if (returnQuoted) {
            result.append('\"');
        }

        return result.toString();
    }

    static String readTokenOrQuotedString(Reader input, boolean returnQuoted)
            throws IOException {

        // Peek at next character to enable correct method to be called
        int c = skipLws(input);

        if (c == '"') {
            return readQuotedString(input, returnQuoted);
        } else {
            return readToken(input);
        }
    }

    /**
     * Token can be read unambiguously with or without surrounding quotes so
     * this parsing method for token permits optional surrounding double quotes.
     * This is not defined in any RFC. It is a special case to handle data from
     * buggy clients (known buggy clients for DIGEST auth include Microsoft IE 8
     * &amp; 9, Apple Safari for OSX and iOS) that add quotes to values that
     * should be tokens.
     *
     * @return the token if one was found, null if data other than a token or
     *         quoted token was found or null if the end of data was reached
     *         before a quoted token was terminated
     */
    static String readQuotedToken(Reader input) throws IOException {

        StringBuilder result = new StringBuilder();
        boolean quoted = false;

        skipLws(input);
        input.mark(1);
        int c = input.read();

        if (c == '"') {
            quoted = true;
        } else if (c == -1 || !isToken(c)) {
            return null;
        } else {
            result.append((char) c);
        }
        input.mark(1);
        c = input.read();

        while (c != -1 && isToken(c)) {
            result.append((char) c);
            input.mark(1);
            c = input.read();
        }

        if (quoted) {
            if (c != '"') {
                return null;
            }
        } else {
            // Use mark(1)/reset() rather than skip(-1) since skip() is a NOP
            // once the end of the String has been reached.
            input.reset();
        }

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     * LHEX can be read unambiguously with or without surrounding quotes so this
     * parsing method for LHEX permits optional surrounding double quotes. Some
     * buggy clients (libwww-perl for DIGEST auth) are known to send quoted LHEX
     * when the specification requires just LHEX.
     *
     * <p>
     * LHEX are, literally, lower-case hexadecimal digits. This implementation
     * allows for upper-case digits as well, converting the returned value to
     * lower-case.
     *
     * @return  the sequence of LHEX (minus any surrounding quotes) if any was
     *          found, or <code>null</code> if data other LHEX was found
     */
    static String readLhex(Reader input) throws IOException {

        StringBuilder result = new StringBuilder();
        boolean quoted = false;

        skipLws(input);
        input.mark(1);
        int c = input.read();

        if (c == '"') {
            quoted = true;
        } else if (c == -1 || !isHex(c)) {
            return null;
        } else {
            if ('A' <= c && c <= 'F') {
                c -= ('A' - 'a');
            }
            result.append((char) c);
        }
        input.mark(1);
        c = input.read();

        while (c != -1 && isHex(c)) {
            if ('A' <= c && c <= 'F') {
                c -= ('A' - 'a');
            }
            result.append((char) c);
            input.mark(1);
            c = input.read();
        }

        if (quoted) {
            if (c != '"') {
                return null;
            }
        } else {
            // Use mark(1)/reset() rather than skip(-1) since skip() is a NOP
            // once the end of the String has been reached.
            input.reset();
        }

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    static double readWeight(Reader input, char delimiter) throws IOException {
        skipLws(input);
        int c = input.read();
        if (c == -1 || c == delimiter) {
            // No q value just whitespace
            return 1;
        } else if (c != 'q') {
            // Malformed. Use quality of zero so it is dropped.
            skipUntil(input, c, delimiter);
            return 0;
        }
        // RFC 7231 does not allow whitespace here but be tolerant
        skipLws(input);
        c = input.read();
        if (c != '=') {
            // Malformed. Use quality of zero so it is dropped.
            skipUntil(input, c, delimiter);
            return 0;
        }

        // RFC 7231 does not allow whitespace here but be tolerant
        skipLws(input);
        c = input.read();

        // Should be no more than 3 decimal places
        StringBuilder value = new StringBuilder(5);
        int decimalPlacesRead = -1;

        if (c == '0' || c == '1') {
            value.append((char) c);
            c = input.read();

            while (true) {
                if (decimalPlacesRead == -1 && c == '.') {
                    value.append('.');
                    decimalPlacesRead = 0;
                } else if (decimalPlacesRead > -1 && c >= '0' && c <= '9') {
                    if (decimalPlacesRead < 3) {
                        value.append((char) c);
                        decimalPlacesRead++;
                    }
                } else {
                    break;
                }
                c = input.read();
            }
        } else {
            // Malformed. Use quality of zero so it is dropped and skip until
            // EOF or the next delimiter
            skipUntil(input, c, delimiter);
            return 0;
        }

        if (c == 9 || c == 32) {
            skipLws(input);
            c = input.read();
        }

        // Must be at delimiter or EOF
        if (c != delimiter && c != -1) {
            // Malformed. Use quality of zero so it is dropped and skip until
            // EOF or the next delimiter
            skipUntil(input, c, delimiter);
            return 0;
        }

        double result = Double.parseDouble(value.toString());
        if (result > 1) {
            return 0;
        }
        return result;
    }


    /**
     * @return If inIPv6 is false, the position of ':' that separates the host
     *         from the port or -1 if it is not present. If inIPv6 is true, the
     *         number of characters read
     */
    static int readHostIPv4(Reader reader, boolean inIPv6) throws IOException {
        int octet = -1;
        int octetCount = 1;
        int c;
        int pos = 0;

        // readAheadLimit doesn't matter as all the readers passed to this
        // method buffer the entire content.
        reader.mark(1);
        do {
            c = reader.read();
            if (c == '.') {
                if (octet > -1 && octet < 256) {
                    // Valid
                    octetCount++;
                    octet = -1;
                } else if (inIPv6 || octet == -1) {
                    throw new IllegalArgumentException(
                            sm.getString("http.invalidOctet", Integer.toString(octet)));
                } else {
                    // Might not be an IPv4 address. Could be a host / FQDN with
                    // a fully numeric component.
                    reader.reset();
                    return readHostDomainName(reader);
                }
            } else if (isNumeric(c)) {
                if (octet == -1) {
                    octet = c - '0';
                } else if (octet == 0) {
                    // Leading zero in non-zero octet. Not valid (ambiguous).
                    if (inIPv6) {
                        throw new IllegalArgumentException(sm.getString("http.invalidLeadingZero"));
                    } else {
                        // Could be a host/FQDN
                        reader.reset();
                        return readHostDomainName(reader);
                    }
                } else {
                    octet = octet * 10 + c - '0';
                }
            } else if (c == ':') {
                break;
            } else if (c == -1) {
                if (inIPv6) {
                    throw new IllegalArgumentException(sm.getString("http.noClosingBracket"));
                } else {
                    pos = -1;
                    break;
                }
            } else if (c == ']') {
                if (inIPv6) {
                    pos++;
                    break;
                } else {
                    throw new IllegalArgumentException(sm.getString("http.closingBracket"));
                }
            } else if (!inIPv6 && (isAlpha(c) || c == '-')) {
                // Go back to the start and parse as a host / FQDN
                reader.reset();
                return readHostDomainName(reader);
            } else {
                throw new IllegalArgumentException(sm.getString(
                        "http.illegalCharacterIpv4", Character.toString((char) c)));
            }
            pos++;
        } while (true);

        if (octetCount != 4 || octet < 0 || octet > 255) {
            // Might not be an IPv4 address. Could be a host name or a FQDN with
            // fully numeric components. Go back to the start and parse as a
            // host / FQDN.
            reader.reset();
            return readHostDomainName(reader);
        }

        return pos;
    }


    /**
     * @return The position of ':' that separates the host from the port or -1
     *         if it is not present
     */
    static int readHostIPv6(Reader reader) throws IOException {
        // Must start with '['
        int c = reader.read();
        if (c != '[') {
            throw new IllegalArgumentException(sm.getString("http.noOpeningBracket"));
        }

        int h16Count = 0;
        int h16Size = 0;
        int pos = 1;
        boolean parsedDoubleColon = false;
        int precedingColonsCount = 0;

        do {
            c = reader.read();
            if (h16Count == 0 && precedingColonsCount == 1 && c != ':') {
                // Can't start with a single :
                throw new IllegalArgumentException(sm.getString("http.singleColonStart"));
            }
            if (HttpParser.isHex(c)) {
                if (h16Size == 0) {
                    // Start of a new h16 block
                    precedingColonsCount = 0;
                    h16Count++;
                }
                h16Size++;
                if (h16Size > 4) {
                    throw new IllegalArgumentException(sm.getString("http.invalidHextet"));
                }
            } else if (c == ':') {
                if (precedingColonsCount >=2 ) {
                    // ::: is not allowed
                    throw new IllegalArgumentException(sm.getString("http.tooManyColons"));
                } else {
                    if(precedingColonsCount == 1) {
                        // End of ::
                        if (parsedDoubleColon ) {
                            // Only allowed one :: sequence
                            throw new IllegalArgumentException(
                                    sm.getString("http.tooManyDoubleColons"));
                        }
                        parsedDoubleColon = true;
                        // :: represents at least one h16 block
                        h16Count++;
                    }
                    precedingColonsCount++;
                    // mark if the next symbol is hex before the actual read
                    reader.mark(4);
                }
                h16Size = 0;
            } else if (c == ']') {
                if (precedingColonsCount == 1) {
                    // Can't end on a single ':'
                    throw new IllegalArgumentException(sm.getString("http.singleColonEnd"));
                }
                pos++;
                break;
            } else if (c == '.') {
                if (h16Count == 7 || h16Count < 7 && parsedDoubleColon) {
                    reader.reset();
                    pos -= h16Size;
                    pos += readHostIPv4(reader, true);
                    h16Count++;
                    break;
                } else {
                    throw new IllegalArgumentException(sm.getString("http.invalidIpv4Location"));
                }
            } else {
                throw new IllegalArgumentException(sm.getString(
                        "http.illegalCharacterIpv6", Character.toString((char) c)));
            }
            pos++;
        } while (true);

        if (h16Count > 8) {
            throw new IllegalArgumentException(
                    sm.getString("http.tooManyHextets", Integer.toString(h16Count)));
        } else if (h16Count != 8 && !parsedDoubleColon) {
            throw new IllegalArgumentException(
                    sm.getString("http.tooFewHextets", Integer.toString(h16Count)));
        }

        c = reader.read();
        if (c == ':') {
            return pos;
        } else {
            if(c == -1) {
                return -1;
            }
            throw new IllegalArgumentException(
                    sm.getString("http.illegalAfterIpv6", Character.toString((char) c)));
        }
    }

    /**
     * @return The position of ':' that separates the host from the port or -1
     *         if it is not present
     */
    static int readHostDomainName(Reader reader) throws IOException {
        DomainParseState state = DomainParseState.NEW;
        int pos = 0;

        while (state.mayContinue()) {
            state = state.next(reader.read());
            pos++;
        }

        if (DomainParseState.COLON == state) {
            // State identifies the state of the previous character
            return pos - 1;
        } else {
            return -1;
        }
    }


    /**
     * Skips all characters until EOF or the specified target is found. Normally
     * used to skip invalid input until the next separator.
     */
    static SkipResult skipUntil(Reader input, int c, char target) throws IOException {
        while (c != -1 && c != target) {
            c = input.read();
        }
        if (c == -1) {
            return SkipResult.EOF;
        } else {
            return SkipResult.FOUND;
        }
    }


    private void relax(boolean[] flags, String relaxedChars) {
        if (relaxedChars != null && relaxedChars.length() > 0) {
            char[] chars = relaxedChars.toCharArray();
            for (char c : chars) {
                if (isRelaxable(c)) {
                    flags[c] = true;
                    IS_NOT_REQUEST_TARGET[c] = false;
                }
            }
        }
    }


    private enum DomainParseState {
        NEW(     true, false, false, false, "http.invalidCharacterDomain.atStart"),
        ALPHA(   true,  true,  true,  true, "http.invalidCharacterDomain.afterLetter"),
        NUMERIC( true,  true,  true,  true, "http.invalidCharacterDomain.afterNumber"),
        PERIOD(  true, false, false,  true, "http.invalidCharacterDomain.afterPeriod"),
        HYPHEN(  true,  true, false, false, "http.invalidCharacterDomain.afterHyphen"),
        COLON(  false, false, false, false, "http.invalidCharacterDomain.afterColon"),
        END(    false, false, false, false, "http.invalidCharacterDomain.atEnd");

        private final boolean mayContinue;
        private final boolean allowsHyphen;
        private final boolean allowsPeriod;
        private final boolean allowsEnd;
        private final String errorMsg;

        private DomainParseState(boolean mayContinue, boolean allowsHyphen, boolean allowsPeriod,
                boolean allowsEnd, String errorMsg) {
            this.mayContinue = mayContinue;
            this.allowsHyphen = allowsHyphen;
            this.allowsPeriod = allowsPeriod;
            this.allowsEnd = allowsEnd;
            this.errorMsg = errorMsg;
        }

        public boolean mayContinue() {
            return mayContinue;
        }

        public DomainParseState next(int c) {
            if (c == -1) {
                if (allowsEnd) {
                    return END;
                } else {
                    throw new IllegalArgumentException(
                            sm.getString("http.invalidSegmentEndState", this.name()));
                }
            } else if (HttpParser.isAlpha(c)) {
                return ALPHA;
            } else if (HttpParser.isNumeric(c)) {
                return NUMERIC;
            } else if (c == '.') {
                if (allowsPeriod) {
                    return PERIOD;
                } else {
                    throw new IllegalArgumentException(sm.getString(errorMsg,
                            Character.toString((char) c)));
                }
            } else if (c == ':') {
                if (allowsEnd) {
                    return COLON;
                } else {
                    throw new IllegalArgumentException(sm.getString(errorMsg,
                            Character.toString((char) c)));
                }
            } else if (c == '-') {
                if (allowsHyphen) {
                    return HYPHEN;
                } else {
                    throw new IllegalArgumentException(sm.getString(errorMsg,
                            Character.toString((char) c)));
                }
            } else {
                throw new IllegalArgumentException(sm.getString(
                        "http.illegalCharacterDomain", Character.toString((char) c)));
            }
        }
    }
}

 

/**
 * An internationalization / localization helper class which reduces
 * the bother of handling ResourceBundles and takes care of the
 * common cases of message formating which otherwise require the
 * creation of Object arrays and such.
 *
 * <p>The StringManager operates on a package basis. One StringManager
 * per package can be created and accessed via the getManager method
 * call.
 *
 * <p>The StringManager will look for a ResourceBundle named by
 * the package name given plus the suffix of "LocalStrings". In
 * practice, this means that the localized information will be contained
 * in a LocalStrings.properties file located in the package
 * directory of the class path.
 *
 * <p>Please see the documentation for java.util.ResourceBundle for
 * more information.
 * 一个国际化/本地化助手类，它减少了处理resourcebundle的麻烦，
 * 并处理消息形式化的常见情况，否则需要创建对象数组等。
 * StringManager基于包进行操作。
 * 可以通过getManager方法调用创建和访问每个包中的一个StringManager。
 * StringManager将查找一个ResourceBundle，
 * 它的名称由给定的包名加上“localstring”后缀命名。
 * 实际上，这意味着本地化信息将包含在localstring中。
 * 属性文件位于类路径的包目录中。
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Mel Martinez [mmartinez@g1440.com]
 * @see java.util.ResourceBundle
 */
class StringManager {

    private static int LOCALE_CACHE_SIZE = 10;

    /**
     * The ResourceBundle for this StringManager.
     */
    private final ResourceBundle bundle;
    private final Locale locale;


    /**
     * Creates a new StringManager for a given package. This is a
     * private method and all access to it is arbitrated by the
     * static getManager method call so that only one StringManager
     * per package will be created.
     * 为给定包创建一个新的StringManager。
     * 这是一个私有方法，对它的所有访问都由静态getManager方法调用仲裁，
     * 因此每个包只创建一个StringManager。
     *
     * @param packageName Name of package to create StringManager for.
     */
    private StringManager(String packageName, Locale locale) {
        String bundleName = packageName + ".LocalStrings";
        ResourceBundle bnd = null;
        try {
            // The ROOT Locale uses English. If English is requested, force the
            // use of the ROOT Locale else incorrect results may be obtained if
            // the system default locale is not English and translations are
            // available for the system default locale.
            // 根语言环境使用英语。如果要求使用英语，则强制使用根语言环境，
            // 否则，如果系统默认语言环境不是英语，并且系统默认语言环境可用翻译，
            // 则可能会得到不正确的结果。
            if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                locale = Locale.ROOT;
            }
            bnd = ResourceBundle.getBundle(bundleName, locale);
        } catch (MissingResourceException ex) {
            // Try from the current loader (that's the case for trusted apps)
            // Should only be required if using a TC5 style classloader structure
            // where common != shared != server
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                try {
                    bnd = ResourceBundle.getBundle(bundleName, locale, cl);
                } catch (MissingResourceException ex2) {
                    // Ignore
                }
            }
        }
        bundle = bnd;
        // Get the actual locale, which may be different from the requested one
        if (bundle != null) {
            Locale bundleLocale = bundle.getLocale();
            if (bundleLocale.equals(Locale.ROOT)) {
                this.locale = Locale.ENGLISH;
            } else {
                this.locale = bundleLocale;
            }
        } else {
            this.locale = null;
        }
    }


    /**
     * Get a string from the underlying resource bundle or return null if the
     * String is not found.
     *
     * @param key to desired resource String
     *
     * @return resource String matching <i>key</i> from underlying bundle or
     *         null if not found.
     *
     * @throws IllegalArgumentException if <i>key</i> is null
     */
    public String getString(String key) {
        if (key == null){
            String msg = "key may not have a null value";
            throw new IllegalArgumentException(msg);
        }

        String str = null;

        try {
            // Avoid NPE if bundle is null and treat it like an MRE
            if (bundle != null) {
                str = bundle.getString(key);
            }
        } catch (MissingResourceException mre) {
            //bad: shouldn't mask an exception the following way:
            //   str = "[cannot find message associated with key '" + key +
            //         "' due to " + mre + "]";
            //     because it hides the fact that the String was missing
            //     from the calling code.
            //good: could just throw the exception (or wrap it in another)
            //      but that would probably cause much havoc on existing
            //      code.
            //better: consistent with container pattern to
            //      simply return null.  Calling code can then do
            //      a null check.
            str = null;
        }

        return str;
    }


    /**
     * Get a string from the underlying resource bundle and format
     * it with the given set of arguments.
     *
     * @param key  The key for the required message
     * @param args The values to insert into the message
     *
     * @return The request string formatted with the provided arguments or the
     *         key if the key was not found.
     */
    public String getString(final String key, final Object... args) {
        String value = getString(key);
        if (value == null) {
            value = key;
        }

        MessageFormat mf = new MessageFormat(value);
        mf.setLocale(locale);
        return mf.format(args, new StringBuffer(), null).toString();
    }


    /**
     * Identify the Locale this StringManager is associated with.
     *
     * @return The Locale associated with the StringManager
     */
    public Locale getLocale() {
        return locale;
    }


    // --------------------------------------------------------------
    // STATIC SUPPORT METHODS
    // --------------------------------------------------------------

    private static final Map<String, Map<Locale,StringManager>> managers =
            new Hashtable<>();


    /**
     * Get the StringManager for a given class. The StringManager will be
     * returned for the package in which the class is located. If a manager for
     * that package already exists, it will be reused, else a new
     * StringManager will be created and returned.
     *
     * @param clazz The class for which to retrieve the StringManager
     *
     * @return The instance associated with the package of the provide class
     */
    public static final StringManager getManager(Class<?> clazz) {
        return getManager(clazz.getPackage().getName());
    }


    /**
     * Get the StringManager for a particular package. If a manager for
     * a package already exists, it will be reused, else a new
     * StringManager will be created and returned.
     *
     * @param packageName The package name
     *
     * @return The instance associated with the given package and the default
     *         Locale
     */
    public static final StringManager getManager(String packageName) {
        return getManager(packageName, Locale.getDefault());
    }


    /**
     * Get the StringManager for a particular package and Locale. If a manager
     * for a package/Locale combination already exists, it will be reused, else
     * a new StringManager will be created and returned.
     *
     * @param packageName The package name
     * @param locale      The Locale
     *
     * @return The instance associated with the given package and Locale
     */
    public static final synchronized StringManager getManager(
            String packageName, Locale locale) {

        Map<Locale,StringManager> map = managers.get(packageName);
        if (map == null) {
            /*
             * Don't want the HashMap to be expanded beyond LOCALE_CACHE_SIZE.
             * Expansion occurs when size() exceeds capacity. Therefore keep
             * size at or below capacity.
             * removeEldestEntry() executes after insertion therefore the test
             * for removal needs to use one less than the maximum desired size
             *
             */
            map = new LinkedHashMap<Locale,StringManager>(LOCALE_CACHE_SIZE, 1, true) {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry(
                        Map.Entry<Locale,StringManager> eldest) {
                    if (size() > (LOCALE_CACHE_SIZE - 1)) {
                        return true;
                    }
                    return false;
                }
            };
            managers.put(packageName, map);
        }

        StringManager mgr = map.get(locale);
        if (mgr == null) {
            mgr = new StringManager(packageName, locale);
            map.put(locale, mgr);
        }
        return mgr;
    }


    /**
     * Retrieve the StringManager for a list of Locales. The first StringManager
     * found will be returned.
     *
     * @param packageName      The package for which the StringManager was
     *                         requested
     * @param requestedLocales The list of Locales
     *
     * @return the found StringManager or the default StringManager
     */
    public static StringManager getManager(String packageName,
            Enumeration<Locale> requestedLocales) {
        while (requestedLocales.hasMoreElements()) {
            Locale locale = requestedLocales.nextElement();
            StringManager result = getManager(packageName, locale);
            if (result.getLocale().equals(locale)) {
                return result;
            }
        }
        // Return the default
        return getManager(packageName);
    }
}


/**
 * @author 单一功能,处理404 网页
 * 
 */

 
 













