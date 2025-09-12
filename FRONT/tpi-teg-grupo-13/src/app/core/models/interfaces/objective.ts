import { ObjectiveType } from "../../enums/objective-type";

export interface Objective {
  id: number;
  type: ObjectiveType;
  description: string;
  targetData: string;
  isCommon : boolean;
  isAchieved :boolean;
  targetContinents: string[];
  targetColor: string | null;
  }
