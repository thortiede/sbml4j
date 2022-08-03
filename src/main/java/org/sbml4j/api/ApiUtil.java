/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.api;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;

public class ApiUtil {
  public static void setExampleResponse(NativeWebRequest req,
                                        String contentType, String example) {
    try {
      HttpServletResponse res =
          req.getNativeResponse(HttpServletResponse.class);
      res.setCharacterEncoding("UTF-8");
      res.addHeader("Content-Type", contentType);
      res.getWriter().print(example);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
