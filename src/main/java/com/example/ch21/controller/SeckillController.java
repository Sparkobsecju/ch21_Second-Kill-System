package com.example.ch21.controller;

import com.example.ch21.exception.InsufficientInventoryException;
import com.example.ch21.exception.OrderInvalidationException;
import com.example.ch21.exception.RepeatSeckillException;
import com.example.ch21.exception.UnpaidException;
import com.example.ch21.service.SeckillService;
import com.example.ch21.persistence.entity.SeckillItem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @GetMapping("/list")
    public String getAllItem(Model model) {
        List<SeckillItem> items = seckillService.getAllItem();
        translateItemImgUrl(items);
        model.addAttribute("item", items);
        return "list";
    }

    @RequestMapping("/{id}")
    public String getItem(Model model, @PathVariable Integer id, String mobile) {
        if (id == null) {
            return "list";
        }
        try {
            SeckillItem item = seckillService.getItemById(id, mobile);
            List<SeckillItem> items = new ArrayList<>();
            items.add(item);
            translateItemImgUrl(items);
            model.addAttribute("item", item);
            model.addAttribute("mobile", mobile);
        } catch (RepeatSeckillException e) {
            model.addAttribute("error", "您已經限時搶購過該商品！");
        } catch (UnpaidException e) {
            model.addAttribute("id", id);
            model.addAttribute("mobile", mobile);
            model.addAttribute("msg", "您已經限時搶購過該商品，但還未支付");
            return "pay";
        } catch (OrderInvalidationException e) {
            model.addAttribute("error", "由於您未支付產品，訂單已經解除");
        }
        return "item";
    }

    @PostMapping("/exec")
    public String execSeckill(Model model, Integer id, String mobile) {
        try {
            seckillService.execSeckill(id, mobile);
            model.addAttribute("id", id);
            model.addAttribute("mobile", mobile);
            model.addAttribute("msg", "限時搶購成功，請在10分鐘內支付");

        } catch (InsufficientInventoryException e) {
            model.addAttribute("error", "商品已經售完！");
        }
        return "pay";
    }

    @GetMapping("/pay")
    @ResponseBody
    public String pay(Integer id, String mobile) {
        try {
            seckillService.pay(id, mobile);
            return "支付成功";
        } catch (OrderInvalidationException e) {
            return "您的訂單已經解除";
        }
    }

    /**
     * 對商品圖片的 URL 進行轉換
     * @param items
     */
    private void translateItemImgUrl(List<SeckillItem> items) {
        for (SeckillItem item: items) {
            item.setImgUrl(getServerInfo() + "/img/" + item.getImageUrl());
        }
    }

    /**
     * 得到後端程式 Context 路徑
     * @return Context 路徑
     */
    private String getServerInfo() {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        StringBuffer sb = new StringBuffer();
        HttpServletRequest request = attrs.getRequest();
        sb.append(request.getContextPath());
        return sb.toString();
    }
}
