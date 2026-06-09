package com.amit.test.github_mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainFile {

    @Autowired
    private Dependency1 dependency1;

    @Autowired
    private Dependency2 dependency2;
}
