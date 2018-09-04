package io.choerodon.asgard.api.controller.v1;

import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.junit.Test;

public class MockStaticByJavassist {


    @Test
    public void mockPageSay() throws Exception{
        modifyPage();

    }

    private void modifyPage() throws Exception{
        ClassPool cp = new ClassPool(true);
        CtClass ctClass = cp.get("io.choerodon.mybatis.pagehelper.PageHelper");
        CtMethod ctMethod = ctClass.getDeclaredMethod("doPageAndSort");
        ctMethod.setBody("{ $2.doSelect(); return null; }");
        ctClass.toClass();
    }

}