package me.coley.recaf.ui.context;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import me.coley.recaf.RecafUI;
import me.coley.recaf.config.Configs;
import me.coley.recaf.mapping.MappingsAdapter;
import me.coley.recaf.ui.dialog.ConfirmDialog;
import me.coley.recaf.ui.dialog.PackageSelectDialog;
import me.coley.recaf.ui.dialog.TextInputDialog;
import me.coley.recaf.ui.util.Icons;
import me.coley.recaf.ui.util.Lang;
import me.coley.recaf.workspace.Workspace;
import me.coley.recaf.workspace.resource.Resource;

import java.util.Optional;
import java.util.TreeSet;

/**
 * Context menu builder for packages <i>(For paths in {@link Resource#getClasses()} ()})</i>.
 *
 * @author Matt Coley
 */
public class PackageContextBuilder extends ContextBuilder {
	private String packageName;

	/**
	 * @param packageName
	 * 		Name of package.
	 *
	 * @return Builder.
	 */
	public PackageContextBuilder setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	@Override
	public ContextMenu build() {
		String name = packageName;
		ContextMenu menu = new ContextMenu();
		// Menu search = menu("menu.search", Icons.ACTION_SEARCH);
		Menu refactor = menu("menu.refactor");
		refactor.getItems().add(action("menu.refactor.move", Icons.ACTION_MOVE, this::move));
		refactor.getItems().add(action("menu.refactor.rename", Icons.ACTION_EDIT, this::rename));
		menu.getItems().add(createHeader(shortenPath(name), Icons.getIconView(Icons.FOLDER_PACKAGE)));
		menu.getItems().add(action("menu.edit.delete", Icons.ACTION_DELETE, this::delete));
		// menu.getItems().add(search);
		menu.getItems().add(refactor);
		// TODO: Package context menu items
		//  - search
		//    - references
		return menu;
	}

	@Override
	public Resource findContainerResource() {
		Workspace workspace = RecafUI.getController().getWorkspace();
		Resource resource = workspace.getResources().getPrimary();
		if (resource.getClasses().keySet().stream().anyMatch(p -> p.startsWith(packageName)))
			return resource;
		for (Resource library : workspace.getResources().getLibraries()) {
			if (library.getClasses().keySet().stream().anyMatch(p -> p.startsWith(packageName)))
				return resource;
		}
		logger.warn("Could not find container resource for package {}", packageName);
		return null;
	}

	private void delete() {
		String name = packageName;
		Resource resource = findContainerResource();
		if (resource != null) {
			if (Configs.display().promptDeleteItem) {
				String title = Lang.get("dialog.title.delete-package");
				String header = String.format(Lang.get("dialog.header.delete-package"), "\n" + name);
				ConfirmDialog deleteDialog = new ConfirmDialog(title, header, Icons.getImageView(Icons.ACTION_DELETE));
				boolean canRemove = deleteDialog.showAndWait().orElse(false);
				if (!canRemove) {
					return;
				}
			}
			for (String className : new TreeSet<>(resource.getClasses().keySet())) {
				if (className.startsWith(name + "/")) {
					boolean removed = resource.getClasses().remove(className) != null;
					if (!removed) {
						logger.warn("Tried to delete class '{}' but failed", name);
					}
				}
			}

		} else {
			logger.error("Failed to resolve containing resource for package '{}'", name);
		}
	}

	private void move() {
		Resource resource = findContainerResource();
		if (resource != null) {
			String title = Lang.get("dialog.title.move-package");
			String header = Lang.get("dialog.header.move-package");
			String originalPackage = packageName;
			PackageSelectDialog packageDialog =
					new PackageSelectDialog(title, header, Icons.getImageView(Icons.ACTION_EDIT));
			packageDialog.populate(resource);
			packageDialog.setCurrentPackage(originalPackage);
			Optional<Boolean> moveResult = packageDialog.showAndWait();
			if (moveResult.isPresent() && moveResult.get()) {
				String localName = originalPackage;
				if (localName.contains("/"))
					localName = localName.substring(localName.lastIndexOf('/') + 1);
				// Create mappings to use for renaming.
				String newHostPackage = packageDialog.getSelectedPackage();
				MappingsAdapter mappings = new MappingsAdapter("RECAF-MOVE", false, false);
				// Add mappings for all classes in the package and sub-packages
				for (String className : resource.getClasses().keySet()) {
					if (className.startsWith(originalPackage + "/")) {
						String newPackage = newHostPackage + "/" + localName;
						String renamedClass = className.replace(originalPackage + "/", newPackage + "/");
						mappings.addClass(className, renamedClass);
					}
				}
				// Update all classes in the resource
				applyMappings(resource, mappings);
			}
		} else {
			logger.error("Failed to resolve containing resource for package '{}'", packageName);
		}
	}

	private void rename() {
		String currentPackage = packageName;
		Resource resource = findContainerResource();
		if (resource != null) {
			String title = Lang.get("dialog.title.rename-package");
			String header = Lang.get("dialog.header.rename-package");
			TextInputDialog renameDialog = new TextInputDialog(title, header, Icons.getImageView(Icons.ACTION_EDIT));
			renameDialog.setName(currentPackage);
			Optional<Boolean> renameResult = renameDialog.showAndWait();
			if (renameResult.isPresent() && renameResult.get()) {
				// Create mappings to use for renaming.
				String newPackage = renameDialog.getName();
				MappingsAdapter mappings = new MappingsAdapter("RECAF-RENAME", false, false);
				// Add mappings for all classes in the package and sub-packages
				for (String className : resource.getClasses().keySet()) {
					if (className.startsWith(currentPackage + "/")) {
						String renamedClass = className.replace(currentPackage + "/", newPackage + "/");
						mappings.addClass(className, renamedClass);
					}
				}
				// Update all classes in the resource
				applyMappings(resource, mappings);
			}
		} else {
			logger.error("Failed to resolve containing resource for package '{}'", packageName);
		}
	}
}