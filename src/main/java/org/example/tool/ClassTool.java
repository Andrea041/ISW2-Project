package org.example.tool;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.controllers.GitExtraction.repository;

public class ClassTool {
    public static List<String> getModifiedClass(RevCommit commit) throws IOException {
        List<String> modifiedClasses = new ArrayList<>();

        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        ObjectReader reader = repository.newObjectReader();

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        ObjectId newTree = commit.getTree();
        newTreeIter.reset(reader, newTree);

        if (commit.getParentCount() > 0) {
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);

            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);


            for(DiffEntry entry : entries) {
                if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClasses.add(entry.getNewPath());
                }
            }
        }

        return modifiedClasses;
    }
}
