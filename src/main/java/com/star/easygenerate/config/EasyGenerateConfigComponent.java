package com.star.easygenerate.config;

import java.util.Objects;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2021/07/11
 */
@State(name = "easyGenerate", storages = {@Storage("easyGenerate.xml")})
public class EasyGenerateConfigComponent implements PersistentStateComponent<EasyGenerateConfig> {

    private EasyGenerateConfig configuration;

    @Nullable
    @Override
    public EasyGenerateConfig getState() {
        if (configuration == null) {
            configuration = new EasyGenerateConfig();
        }
        return configuration;
    }

    @Override
    public void loadState(@NotNull EasyGenerateConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
