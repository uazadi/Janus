package it.example.helloword;

import org.eclipse.jface.wizard.Wizard;

public class HelloWizard extends Wizard {

    protected MyPageOne one;
    protected MyPageTwo two;
    
    private String projectPath;

    public HelloWizard(String projectPath) {
        super();
        setNeedsProgressMonitor(true);
        this.projectPath = projectPath;
    }

    @Override
    public String getWindowTitle() {
        return "Export My Data";
    }

    @Override
    public void addPages() {
        one = new MyPageOne(projectPath);
        two = new MyPageTwo();
        addPage(one);
        addPage(two);
    }

    @Override
    public boolean performFinish() {
        // Print the result to the console
        System.out.println(one.getText1());
        System.out.println(two.getText1());

        return true;
    }
	

}
