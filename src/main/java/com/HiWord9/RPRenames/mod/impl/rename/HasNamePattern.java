package com.HiWord9.RPRenames.mod.impl.rename;

import java.util.regex.Pattern;

public interface HasNamePattern {
    String getOriginalNamePattern();
    Pattern getNamePattern();
}
