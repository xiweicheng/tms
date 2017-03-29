import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogHistoryDiff {

    showHandler() {}

    approveHandler() {

    }

    show(f, s, fIndex, sIndex) {
        this.f = f;
        this.s = s;
        this.fIndex = fIndex;
        this.sIndex = sIndex;
        this.diffHtml = utils.diffS(s.content, f.content);
        this.emModal.show({ hideOnApprove: true, autoDimmer: false });
    }
}
