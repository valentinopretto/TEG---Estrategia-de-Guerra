import { Directive, ElementRef, Input, OnChanges, Renderer2 } from '@angular/core';
import {DummyType} from '../../core/enums/dummy-type';

@Directive({
  selector: '[appHighlightDummy]',
})
export class HighlightDummyDirective implements OnChanges {
  @Input() appHighlightDummy!: DummyType; // Cambiado el nombre del Input

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(): void {
    if (this.appHighlightDummy === DummyType.PREMIUM) {
      this.renderer.setStyle(this.el.nativeElement, 'color', 'gold');
      this.renderer.setStyle(this.el.nativeElement, 'fontWeight', 'bold');
    } else {
      this.renderer.removeStyle(this.el.nativeElement, 'color');
      this.renderer.removeStyle(this.el.nativeElement, 'fontWeight');
    }
  }
}
