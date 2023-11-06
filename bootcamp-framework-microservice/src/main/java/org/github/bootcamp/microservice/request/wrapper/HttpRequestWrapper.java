package org.github.bootcamp.microservice.request.wrapper;

import com.alibaba.nacos.common.utils.CollectionUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.github.bootcamp.tooltik.HttpUtil;
import org.github.bootcamp.tooltik.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.util.WebUtils;

/**
 * @author zhuling
 */
@Slf4j
public class HttpRequestWrapper extends HttpServletRequestWrapper {

  @Nullable private final ByteArrayOutputStream byteArrayOutputStream;

  private Map<String, String[]> formFields;

  public HttpRequestWrapper(HttpServletRequest request) {
    super(request);
    this.byteArrayOutputStream = new ByteArrayOutputStream();
    this.formFields = new HashMap<>();
    cacheData();
  }

  @Override
  public ServletInputStream getInputStream() {
    assert byteArrayOutputStream != null;
    return new HttpRequestWrapperInputStream(byteArrayOutputStream.toByteArray());
  }

  @Override
  public String getCharacterEncoding() {
    String enc = super.getCharacterEncoding();
    return (enc != null ? enc : WebUtils.DEFAULT_CHARACTER_ENCODING);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
  }

  @Override
  public String getParameter(String name) {
    String value = null;
    if (HttpUtil.isFormPost(this.getContentType(), this.getMethod())) {
      String[] values = formFields.get(name);
      if (null != values && values.length > 0) {
        value = values[0];
      }
    }

    if (StringUtils.isEmpty(value)) {
      value = super.getParameter(name);
    }

    return value;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    if (HttpUtil.isFormPost(this.getContentType(), this.getMethod())
        && !CollectionUtils.sizeIsEmpty(formFields)) {
      return formFields;
    }

    return super.getParameterMap();
  }

  @Override
  public Enumeration<String> getParameterNames() {
    if (HttpUtil.isFormPost(this.getContentType(), this.getMethod())
        && !CollectionUtils.sizeIsEmpty(formFields)) {
      return Collections.enumeration(formFields.keySet());
    }

    return super.getParameterNames();
  }

  @Override
  public String[] getParameterValues(String name) {
    if (HttpUtil.isFormPost(this.getContentType(), this.getMethod())
        && !CollectionUtils.sizeIsEmpty(formFields)) {
      return formFields.get(name);
    }
    return super.getParameterValues(name);
  }

  private void cacheData() {
    try {
      if (HttpUtil.isFormPost(this.getContentType(), this.getMethod())) {
        this.formFields = super.getParameterMap();
        return;
      }
      ServletInputStream inputStream = super.getInputStream();
      IOUtils.copy(inputStream, this.byteArrayOutputStream);
    } catch (IOException e) {
      log.warn("[HttpRequestWrapper:cacheData], error: {}", e.getMessage());
    }
  }

  private static class HttpRequestWrapperInputStream extends ServletInputStream {
    private final ByteArrayInputStream inputStream;

    public HttpRequestWrapperInputStream(byte[] bytes) {
      this.inputStream = new ByteArrayInputStream(bytes);
    }

    @Override
    public int read() {
      return this.inputStream.read();
    }

    @Override
    public int readLine(byte[] b, int off, int len) {
      return this.inputStream.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
      return this.inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {}
  }
}
