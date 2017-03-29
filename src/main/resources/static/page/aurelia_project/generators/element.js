import {inject} from 'aurelia-dependency-injection';
import {Project, ProjectItem, CLIOptions, UI} from 'aurelia-cli';

@inject(Project, CLIOptions, UI)
export default class ElementGenerator {
  constructor(project, options, ui) {
    this.project = project;
    this.options = options;
    this.ui = ui;
  }

  execute() {
    return this.ui
      .ensureAnswer(this.options.args[0], 'What would you like to call the custom element?')
      .then(name => {
        let fileName = this.project.makeFileName(name);
        let className = this.project.makeClassName(name);

        this.project.elements.add(
          ProjectItem.text(`${fileName}.js`, this.generateJSSource(className)),
          ProjectItem.text(`${fileName}.html`, this.generateHTMLSource(className, name)),
          ProjectItem.text(`${fileName}.less`, this.generateCssSource(name))
        );

        return this.project.commitChanges()
          .then(() => this.ui.log(`Created ${fileName}.`));
      });
  }

  generateJSSource(className) {
return `import { bindable, containerless } from 'aurelia-framework';

@containerless
export class ${className} {

    @bindable value;

    valueChanged(newValue, oldValue) {

    }
}

`;
  }

  generateHTMLSource(className, name) {
return `<template>
  <require from="./${name}.css"></require>
  <div class="${name}">
    <h1>\${value}</h1>
  </div>
</template>`
  }

  generateCssSource(className) {
    return `.${className} {}`;
  }

}
