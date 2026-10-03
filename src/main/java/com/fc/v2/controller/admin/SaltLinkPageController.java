package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.exception.salt.SaltLinkPageRuleException;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.custom.SaltLinkPageBoard;
import com.fc.v2.model.custom.SaltLinkPageForm;
import com.fc.v2.model.custom.SaltLinkPageQuery;
import com.fc.v2.model.custom.SaltLinkSaveResult;
import com.fc.v2.model.custom.SaltLinkScope;
import com.fc.v2.service.ITSaltLinkPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业品种挂接簿页 Controller。
 * 接口层不另摆判路：合不合、欠挂几、露哪几页、三栏计数全由服务层保存/查询方法定夺。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "企业品种挂接簿页")
@Controller
@RequestMapping("/SaltLinkPageController")
public class SaltLinkPageController extends BaseController {

    private final String prefix = "admin/saltLinkPage";

    @Autowired
    private ITSaltLinkPageService saltLinkPageService;

    /** 规则毛病原样转回执，接口层不加工判定 */
    @ExceptionHandler(SaltLinkPageRuleException.class)
    @ResponseBody
    public AjaxResult handleRule(SaltLinkPageRuleException e) {
        return AjaxResult.error(e.getMessage());
    }

    @ApiOperation(value = "名录页跳转", notes = "名录页跳转")
    @GetMapping("/view")
    @RequiresPermissions("salt:saltLinkPage:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "挂接簿页名录页查询", action = "list")
    @ApiOperation(value = "名录页一屏", notes = "本层属地该露的页与三栏计数，同一次算")
    @GetMapping("/list")
    @RequiresPermissions("salt:saltLinkPage:list")
    @ResponseBody
    public Map<String, Object> list(SaltLinkPageQuery query) {
        // 一屏摆多少条由处里定：page/limit 由屏上带进
        SaltLinkPageBoard board = saltLinkPageService.loadBoard(query);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "");
        // 在册页数与逐页点出来的出自同一次算
        resp.put("count", board.getTotalCount());
        resp.put("data", board.getRows());
        resp.put("registerCount", board.getTotalCount());
        resp.put("doneCount", board.getDoneCount());
        resp.put("heldCount", board.getHeldCount());
        return resp;
    }

    @ApiOperation(value = "起页跳转", notes = "起页跳转")
    @GetMapping("/add")
    @RequiresPermissions("salt:saltLinkPage:add")
    public String add(ModelMap model) {
        model.put("roster", saltLinkPageService.rosterList());
        return prefix + "/add";
    }

    @Log(title = "挂接簿页起页（表单来路）", action = "add")
    @ApiOperation(value = "起页（来路一）", notes = "屏上起页表单")
    @PostMapping("/add")
    @RequiresPermissions("salt:saltLinkPage:add")
    @ResponseBody
    public AjaxResult add(SaltLinkPageForm form) {
        SaltLinkSaveResult r = saltLinkPageService.submitForm(form, SaltLinkScope.FORM);
        return AjaxResult.success(200, r.getMessage(), r);
    }

    @Log(title = "挂接簿页起页（另一条来路）", action = "open")
    @ApiOperation(value = "起页（来路二）", notes = "另一种起页走法，同一套判定")
    @PostMapping("/open")
    @RequiresPermissions("salt:saltLinkPage:add")
    @ResponseBody
    public AjaxResult open(SaltLinkPageForm form) {
        // 两条来路按同一套说法办，屏上不长第二种结果
        SaltLinkSaveResult r = saltLinkPageService.submitForm(form, SaltLinkScope.OPEN);
        return AjaxResult.success(200, r.getMessage(), r);
    }

    @ApiOperation(value = "改页跳转", notes = "改页跳转")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("salt:saltLinkPage:edit")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
        model.put("page", saltLinkPageService.selectBoardVoById(id));
        return prefix + "/edit";
    }

    @Log(title = "挂接簿页改页", action = "edit")
    @ApiOperation(value = "改页保存", notes = "已挂一变/换层重算欠挂")
    @PostMapping("/edit")
    @RequiresPermissions("salt:saltLinkPage:edit")
    @ResponseBody
    public AjaxResult editSave(SaltLinkPageForm form) {
        SaltLinkSaveResult r = saltLinkPageService.submitForm(form, SaltLinkScope.FORM);
        return AjaxResult.success(200, r.getMessage(), r);
    }

    @Log(title = "挂接簿页删除", action = "remove")
    @ApiOperation(value = "删除", notes = "软删")
    @PostMapping("/remove")
    @RequiresPermissions("salt:saltLinkPage:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(saltLinkPageService.deleteTSaltLinkPageByIds(ids));
    }
}
