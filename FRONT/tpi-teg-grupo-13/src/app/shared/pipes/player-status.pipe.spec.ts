import { PlayerStatusPipe } from './player-status.pipe';

describe('PlayerStatusPipe', () => {
  it('create an instance', () => {
    const pipe = new PlayerStatusPipe();
    expect(pipe).toBeTruthy();
  });
});
