package org.example.tool;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassTool {
    private ClassTool() {}

    public static List<String> getModifiedClass(RevCommit commit, Repository repository) throws IOException {
        List<String> modifiedClasses = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser newTree = new CanonicalTreeParser();

            ObjectId objectIdNew = commit.getTree();
            newTree.reset(reader, objectIdNew);
            RevCommit commitParent = commit.getParent(0);

            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            ObjectId objectIdOld = commitParent.getTree();
            oldTree.reset(reader, objectIdOld);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTree, newTree);

            for(DiffEntry entry : entries) {
                if(entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/"))
                    modifiedClasses.add(entry.getNewPath());
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore
        }

        return modifiedClasses;
    }
}
