/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativebinaries.toolchain.internal.gcc;

import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.internal.tasks.compile.ArgCollector;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.api.tasks.WorkResult;
import org.gradle.nativebinaries.language.c.internal.CCompileSpec;
import org.gradle.nativebinaries.toolchain.internal.CommandLineTool;

import java.io.File;

class CCompiler implements Compiler<CCompileSpec> {

    private final CommandLineTool<CCompileSpec> commandLineTool;

    public CCompiler(CommandLineTool<CCompileSpec> commandLineTool, boolean useCommandFile) {
        GccSpecToArguments<CCompileSpec> specToArguments = new GccSpecToArguments<CCompileSpec>(
                new CCompileSpecToArguments(),
                useCommandFile
        );
        this.commandLineTool = commandLineTool.withArguments(specToArguments);
    }

    public WorkResult execute(CCompileSpec spec) {
        boolean didRemove = false;
        boolean didCompile = false;
        for (File removedSource : spec.getRemovedSourceFiles()) {
            didRemove |= deleteOutputForRemovedSource(spec.getObjectFileDir(), removedSource);
        }
        if (!spec.getSourceFiles().isEmpty()) {
            didCompile = commandLineTool.inWorkDirectory(spec.getObjectFileDir()).execute(spec).getDidWork();
        }
        return new SimpleWorkResult(didRemove || didCompile);
    }

    private boolean deleteOutputForRemovedSource(File objectFileDir, File removedSource) {
        String objectFileName = removedSource.getName().replaceFirst("\\.[^\\.]+$", ".o");
        File objectFile = new File(objectFileDir, objectFileName);
        return objectFile.delete();
    }

    private static class CCompileSpecToArguments extends CommonGccCompileSpecToArguments<CCompileSpec> {
        @Override
        public void collectArguments(CCompileSpec spec, ArgCollector collector) {
            // C-compiling options
            collector.args("-x", "c");

            super.collectArguments(spec, collector);
        }
    }
}
