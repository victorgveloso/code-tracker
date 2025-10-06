package org.codetracker.blame.convertor;

import org.codetracker.blame.convertor.impl.BlameDiffer;
import org.codetracker.blame.convertor.impl.NoBlameDiffer;
import org.codetracker.blame.convertor.impl.util.CsvWriter;
import org.codetracker.blame.convertor.impl.util.StatsCollector;
import org.codetracker.blame.convertor.model.BlameCaseInfo;
import org.codetracker.blame.convertor.model.BlameDifferResult;
import org.codetracker.blame.convertor.model.CodeLinePredicate;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogServiceWithSerialization;
import org.codetracker.blame.impl.CliGitBlameCustomizable;
import org.codetracker.blame.impl.FileTrackerBlameWithSerialization;
import org.codetracker.blame.model.IBlame;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.astDiff.utils.URLHelper;
import org.refactoringminer.util.GitServiceImpl;

import java.util.*;
import java.util.function.Predicate;

import static org.codetracker.blame.util.Utils.*;

/* Created by pourya on 2024-07-14*/
public class BlameDifferDriver {
    private static final GitService gitService = new GitServiceImpl();
    private static final String REPOS_PATH = System.getProperty("user.dir") + "/tmp";
    private static Set<IBlameTool> blamerFactories =
            new LinkedHashSet<IBlameTool>()
                    {{
                    add(BlamersEnum.JGitBlameWithFollow);
                    add(BlamersEnum.JGitBlameHistogramWithFollow);
                    add(BlamersEnum.CliGitBlameIgnoringWhiteSpace);
                    add(BlamersEnum.CliGitBlameDefault);
                    add(BlamersEnum.CliGitBlameMoveAware);
                    add(BlamersEnum.CliGitBlameMoveAwareIgnoringWhiteSpace);
                    add(BlamersEnum.CliGitBlameCopyAware);
                    add(BlamersEnum.CliGitBlameCopyAwareIgnoringWhiteSpace);
                    add(BlamersEnum.FileTrackerBlame);
                    add(BlamersEnum.LHBlame);
                    add(BlamersEnum.LBlame);
                    add(BlamersEnum.HistBlame);
                    add(BlamersEnum.MyersBlame);
                    add(BlamersEnum.HistBlameIW);
                    add(BlamersEnum.MyersBlameIW);
                    }};

    public static final Predicate<String> codeElementIgnoreCondition =
            CodeLinePredicate.BLANK_LINE.or(CodeLinePredicate.OPENING_AND_CLOSING_CURLY_BRACKET);
    private static final BlameDiffer blameDiffer =
//            new BlameDifferOneWithMany(blamerFactories, BlamersEnum.FileTrackerBlame, codeElementIgnoreCondition);
            new NoBlameDiffer(blamerFactories, codeElementIgnoreCondition)
            ;

    private static final String[][] dummies = {
//            {"https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3", "src/main/java/com/puppycrawl/tools/checkstyle/Checker.java"},
            {"https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3", "src/main/java/com/puppycrawl/tools/checkstyle/TreeWalker.java"},
            {"https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3", "src/main/java/com/puppycrawl/tools/checkstyle/checks/coding/FinalLocalVariableCheck.java"},
//            {"https://github.com/javaparser/javaparser/commit/97555053af3025556efe1a168fd7943dac28a2a6", "javaparser-core/src/main/java/com/github/javaparser/printer/lexicalpreservation/Difference.java"},
//            {"https://github.com/javaparser/javaparser/commit/97555053af3025556efe1a168fd7943dac28a2a6", "javaparser-symbol-solver-core/src/main/java/com/github/javaparser/symbolsolver/javaparsermodel/contexts/MethodCallExprContext.java"},
//            {"https://github.com/spring-projects/spring-framework/commit/b325c74216fd9564a36602158fa1269e2e832874", "spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodProcessor.java"},
//            {"https://github.com/junit-team/junit5/commit/77cfe71e7f787c59626198e25350545f41e968bd", "junit-jupiter-engine/src/main/java/org/junit/jupiter/engine/descriptor/ClassTestDescriptor.java"},
//            {"https://github.com/hibernate/hibernate-orm/commit/8bd79b29cfa7b2d539a746dc356d60b66e1e596b", "hibernate-core/src/main/java/org/hibernate/cfg/AnnotationBinder.java"},
//            {"https://github.com/eclipse/jgit/commit/bd1a82502680b5de5bf86f6c4470185fd1602386", "org.eclipse.jgit/src/org/eclipse/jgit/internal/storage/pack/PackWriter.java"},
//            {"https://github.com/JetBrains/intellij-community/commit/ecb1bb9d4d484ae63ee77f8ad45bdce154db9356", "java/compiler/impl/src/com/intellij/compiler/CompilerManagerImpl.java"},
//            {"https://github.com/JetBrains/intellij-community/commit/ecb1bb9d4d484ae63ee77f8ad45bdce154db9356", "java/compiler/impl/src/com/intellij/compiler/actions/CompileDirtyAction.java"}
            {"https://github.com/Alluxio/alluxio/commit/9aeefcd8120bb3b89cdb437d8c32d2ed84b8a825", "servers/src/main/java/tachyon/worker/block/allocator/MaxFreeAllocator.java"},
            {"https://github.com/pouryafard75/DiffBenchmark/commit/5b33dc6f8cfcf8c0e31966c035b0406eca97ec76", "src/main/java/dat/MakeIntels.java"},
            {"https://github.com/apache/flink/commit/9e936a5f8198b0059e9b5fba33163c2bbe3efbdd", "flink-streaming-java/src/main/java/org/apache/flink/streaming/api/datastream/DataStream.java"},
            {"https://github.com/apache/commons-lang/commit/a36c903d4f1065bc59f5e6d2bb0f9d92a5e71d83", "src/main/java/org/apache/commons/lang3/time/DateUtils.java"},
            {"https://github.com/apache/commons-lang/commit/a36c903d4f1065bc59f5e6d2bb0f9d92a5e71d83", "src/main/java/org/apache/commons/lang3/time/DurationFormatUtils.java"},
            {"https://github.com/apache/flink/commit/9e936a5f8198b0059e9b5fba33163c2bbe3efbdd", "flink-runtime/src/main/java/org/apache/flink/runtime/dispatcher/DispatcherRestEndpoint.java"},
            {"https://github.com/junit-team/junit4/commit/02c328028b4d32c15bbf0becc9838e54ecbafcbf", "src/main/java/org/junit/runners/BlockJUnit4ClassRunner.java"},
            {"https://github.com/square/okhttp/commit/5224f3045ba9b171fce521777edf389f9206173c", "okhttp/src/main/java/okhttp3/internal/http2/Http2Connection.java"},
            {"https://github.com/apache/hadoop/commit/9c3fc3ef2865164aa5f121793ac914cfeb21a181", "hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/org/apache/hadoop/yarn/server/resourcemanager/scheduler/fifo/FifoScheduler.java"},
            {"https://github.com/mockito/mockito/commit/077562ea54f1fa87ff8dd233c3060ddbf0f1ce26", "src/main/java/org/mockito/internal/invocation/MatchersBinder.java"},
            {"https://github.com/hibernate/hibernate-search/commit/5b778035965d7588ad1d1ae522c4bafebd3a0e16", "engine/src/main/java/org/hibernate/search/backend/impl/StreamingOperationDispatcher.java"},
            {"https://github.com/eclipse/jetty.project/commit/fc5dd874f3deda71e6cd42af994a5af5cb6be4af", "jetty-http2/http2-server/src/main/java/org/eclipse/jetty/http2/server/HttpTransportOverHTTP2.java"},
            {"https://github.com/pmd/pmd/commit/d528dcd5d45582229ab3410deb7c40b2143d015d", "pmd-java/src/main/java/net/sourceforge/pmd/lang/java/typeresolution/ClassTypeResolver.java"},
            {"https://github.com/apache/tomcat/commit/dbc805e237b98834a7b7afb6da7be44da428c399", "java/org/apache/coyote/http2/ConnectionSettings.java"},
            {"https://github.com/apache/ant/commit/2943e6c208b4152e8d3142168c67a3a23509ba2e", "proposal/myrmidon/src/test/org/apache/myrmidon/components/property/test/DefaultPropertyResolverTestCase.java"},
            {"https://github.com/apache/ant/commit/f382fa32eec7260111db0d67ae9c90dee2d6de0b", "proposal/myrmidon/src/java/org/apache/aut/vfs/impl/DefaultProviderContext.java"},
            {"https://github.com/apache/ant/commit/a5598c47d123f149d442fde20753321ab39658eb", "proposal/myrmidon/src/main/org/apache/tools/todo/taskdefs/perforce/P4Submit.java"},
            {"https://github.com/apache/ant/commit/3244e244c928cd5372c33e30e0605ed69b35af49", "proposal/myrmidon/src/todo/org/apache/tools/ant/util/regexp/JakartaOroRegexp.java"},
            {"https://github.com/apache/ant/commit/a5598c47d123f149d442fde20753321ab39658eb", "proposal/myrmidon/src/main/org/apache/tools/todo/util/regexp/JakartaOroRegexp.java"},
            {"https://github.com/apache/ant/commit/a5598c47d123f149d442fde20753321ab39658eb", "proposal/myrmidon/src/main/org/apache/tools/todo/util/regexp/Jdk14RegexpRegexp.java"},
            {"https://github.com/apache/ant/commit/a5598c47d123f149d442fde20753321ab39658eb", "proposal/myrmidon/src/main/org/apache/tools/todo/util/regexp/JakartaRegexpRegexp.java"},
    };
    public static void main(String[] args) throws Exception {
        // TODO: We want to record the cases that some configuration reduce the number of disagreement
        //  For these cases, Increase the threshold
        //  1000 ?  try to get character numbers from moved methods directly from RefMiner -?
        //  experiment with https://github.com/junit-team/junit4/commit/7a3e99635d7ffcc4d730f27835eeaeb082003199
        int low = 1;
        int high = 62;
        int step = 15;
        blamerFactories.addAll(makeCliGitBlames("-M", low, high, step, false));
        blamerFactories.addAll(makeCliGitBlames("-C", low, high, step, false));
        blamerFactories.addAll(makeCliGitBlames("-M", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-C", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-CC", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-CCC", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-M10CCC", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-M10CC", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-M10C", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-C10M", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-CC10M", low, high, step, true));
        blamerFactories.addAll(makeCliGitBlames("-CCC10M", low, high, step, true));
        run(getDummyBlameCases());
        FileTrackerBlameWithSerialization.saveCacheToFile();
        RMDrivenGitLogServiceWithSerialization.saveCacheToFile();
    }



    public static Set<IBlameTool> makeCliGitBlames(String firstParam, int lowerLimit, int higherLimit, int step, boolean ignoreWhitespace) {
        Set<IBlameTool> iBlameTools = new LinkedHashSet<>();
        int num;
        for (int i = lowerLimit; i <= higherLimit; i+=step) {
            num = i;
            String[] moreOptions = {firstParam + num};
            iBlameTools.add(getCliBlameTool(ignoreWhitespace, moreOptions));
        }
        return iBlameTools;
    }

    public static IBlameTool getCliBlameTool(boolean ignoreWhitespace, String[] moreOptions) {
        IBlame cliGitBlameCustomizable = new CliGitBlameCustomizable(ignoreWhitespace, moreOptions);
        IBlameTool customBlameTool = new IBlameTool() {
            @Override
            public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath) throws Exception {
                return cliGitBlameCustomizable.blameFile(repository, commitId, filePath);
            }

            @Override
            public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
                return cliGitBlameCustomizable.blameFile(repository, commitId, filePath, fromLine, toLine);
            }

            @Override
            public String getToolName() {
                return "CliGitBlameCustomizable" + Arrays.toString(moreOptions) + "IgnoreWhitespace" + ignoreWhitespace;
            }

            @Override
            public String toString() {
                return getToolName();
            }
        };
        return customBlameTool;
    }


    private static Set<BlameCaseInfo> getDummyBlameCases() {
        Set<BlameCaseInfo> result = new LinkedHashSet<>();
        for (String[] dummy : dummies) {
            result.add(new BlameCaseInfo(dummy[0], dummy[1]));
        }
        return result;
    }

    static void run(Set<BlameCaseInfo> blameCases) throws Exception {
        StatsCollector statsCollector = new StatsCollector();
        for (BlameCaseInfo blameCase : blameCases) {
            System.out.println("Processing " + blameCase.url);
            process(blameCase.url, blameCase.filePath, statsCollector);
        }

    }
    public static void process(String url, String filePath, StatsCollector statsCollector) throws Exception {
        {
            String commitId = URLHelper.getCommit(url);
            String owner = getOwner(url);
            String project = getProject(url);
            Repository repository = getRepository(url, gitService, REPOS_PATH);
            BlameDifferResult blameDifferResult = blameDiffer.diff(repository, commitId, filePath);
            Map<Integer, Map<IBlameTool, String>> result = blameDifferResult.getTable();
            statsCollector.process(result, blameDifferResult.getLegitSize(), blameDifferResult.getCodeElementWithReprMap());
            CsvWriter csvWriter = new CsvWriter(owner, project, commitId, filePath, blameDifferResult.getCodeElementWithReprMap());
            csvWriter.writeToCSV(result);
//            csvWriter.writeCertificateToCSV(blameDiffer.getCertificate());
            statsCollector.writeInfo();
        }
    }
}
