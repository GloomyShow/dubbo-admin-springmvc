package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 594829 on 2015/12/24.
 */
@Controller
@RequestMapping("/serviceManager")
public class IndexController {

        private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

        @Autowired
        private HttpServletRequest request;

        @Autowired
        private ProviderService providerService;

        @Autowired
        private ConsumerService consumerService;

       /* public void execute(Context context) {
            Set<String> applications = new HashSet<String>();
            Set<String> services  = new HashSet<String>();
            List<Provider> pList = new ArrayList<Provider>();
            try {
                pList = providerService.findAll();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            for (Provider p : pList) {
                applications.add(p.getApplication());
                services.add(p.getService());
            }
            List<Consumer> cList  = new ArrayList<Consumer>();
            try {
                cList = consumerService.findAll();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            for (Consumer c : cList) {
                applications.add(c.getApplication());
                services.add(c.getService());
            }
            context.put("rootContextPath", new RootContextPath(request.getContextPath()));
            context.put("services", services.size());
            context.put("providers", pList.size());
            context.put("consumers", cList.size());
            context.put("applications", applications.size());
        }*/

}


