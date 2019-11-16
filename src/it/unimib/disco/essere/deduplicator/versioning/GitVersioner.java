package it.unimib.disco.essere.deduplicator.versioning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitVersioner extends Versioner {
	
	Git repo;
	LinkedList<RevCommit> commitHistory;

	public GitVersioner(IJavaProject verionedProject) throws VersionerException {
		super(verionedProject);

		File projectPath = new File(getProjectPath(verionedProject));

		commitHistory = new LinkedList<RevCommit>();
		
		verionedProject.getPath().toFile();

		try {
			repo = Git.open(projectPath);
		} catch (IOException e2) {
			try {
				Git.init().setDirectory(projectPath).call();
				
				repo = Git.open(projectPath);
				
				List<String> files = new ArrayList<String>();
				files.addAll(repo.status().call().getModified());
				files.addAll(repo.status().call().getUntracked());
				
				this.commit(files);
				
				
			} catch (IllegalStateException | GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		

	}


	private String getProjectPath(IJavaProject verionedProject) {
		String projectPath = "";
		try {
			String name = verionedProject.getElementName();
			String[] cp = JavaRuntime.computeDefaultRuntimeClassPath (verionedProject);
			int index = cp[0].lastIndexOf(name);
			projectPath = cp[0].substring(0, index) + name + "/";
		} catch (CoreException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		return projectPath;
	}


	@Override
	public void commit(List<String> compilationUnitToCommit) throws VersionerException {
		try {
			CommitCommand commit = repo.commit();
			
			
			String commitMessage = "[Code Clone Refactoring] Files involved:";
			
			// The files are to be searched between the one modified
			// and the new one created (i.e. the Untracked ones)
			List<String> files = new ArrayList<String>();
			files.addAll(repo.status().call().getModified());
			files.addAll(repo.status().call().getUntracked());
			
			for(String name: compilationUnitToCommit) {
				for(String file: files) {

					if(name.contains(file)) {
						
						repo.add().addFilepattern(file).call();
						
						commit.setOnly(file);
						commitMessage += name + ", "; 
					}
				}
			}

			commitMessage = commitMessage.substring(0, 
					commitMessage.length() - 1) 
					+ ";";
			
			Thread.sleep(100);

			RevCommit revCommit = commit.setMessage(commitMessage).call();
			commitHistory.add(revCommit);
		} catch (NoWorkTreeException | GitAPIException | InterruptedException e) {
			throw new VersionerException(e.getMessage());
		}
	}

	@Override
	public void rollback() throws VersionerException {
		try {
			repo.revert().include(commitHistory.pollLast()).call();
		}catch(Exception e) {
			 throw new VersionerException(e.getMessage());
		}
	}

	@Override
	public Object getRepo() {
		return repo;
	}


	@Override
	public void newBranch(String branchName) throws VersionerException {
		try {	
			applyCheckout(branchName);
		} catch(RefNotFoundException e1) {
			try {
				repo.commit().setMessage("Initial commit").call();
				applyCheckout(branchName);
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		} 
		catch (GitAPIException e) {
			// TODO Auto-generated catch block
			// TODO Handle case branch is already created!
			throw new VersionerException(e.getMessage());
		}
	}
	
	public void newBranch() throws VersionerException{
		this.newBranch(Versioner.DEFAULT_BRANCH_NAME);
	}


	private void applyCheckout(String branchName) 
			throws GitAPIException, RefAlreadyExistsException, RefNotFoundException,
				   InvalidRefNameException, CheckoutConflictException {
		try {
			repo.checkout()
			.setCreateBranch(true)	
			.setName(branchName)
			.call();
		} catch (RefAlreadyExistsException e) {
			repo.checkout()	
			.setName(branchName)
			.call();
		}
	}

}
