package com.hiword9.rprenames.mod.util;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.rename.HasVisibility;
import com.hiword9.rprenames.api.ext.rename.Visibility;

import static com.hiword9.rprenames.util.Util.config;

public class VisibilityHelper {
    public static boolean shouldBeVisible(Rename rename) {
        return config().showHiddenRenames || !isHidden(rename);
    }

    public static boolean isHidden(Rename rename) {
        return rename instanceof HasVisibility v
                && v.getVisibility() == Visibility.HIDDEN;
    }
}
