import { ChangeDetectorRef, Component, OnInit, NgZone } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { SafePipe } from '../shared/safe.pipe';
import { AuthService } from '../shared/auth.service';

@Component({
  selector: 'app-courses-learn-view',
  standalone: true,
  imports: [CommonModule, SafePipe, RouterModule],
  templateUrl: './courses-learn-view.html',
  styleUrls: ['./courses-learn-view.css']
})
export class LessonViewComponent implements OnInit {
  finalScore: number = 0;
  totalInteractives: number = 0;
  isSubmitted: boolean = false;

  levelId: string | null = null;
  courseId: string | null = null;
  sections: any[] = [];
  exercises: any[] = [];
  activeSection: any = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const newLevelId = params.get('levelId');
      const newCourseId = params.get('courseId');
      const newSectionId = params.get('sectionId');

      this.levelId = newLevelId;

      if (newCourseId !== this.courseId) {
        this.courseId = newCourseId;
        this.loadSections(newSectionId);
      } else if (newSectionId) {
        this.updateActiveSection(newSectionId);
        this.loadExercisesForSection(newSectionId);
      }
    });
  }

  loadSections(currentSectionId: string | null) {
    this.http.get<any[]>(`http://localhost:8081/courses/${this.courseId}/sections`)
      .subscribe(data => {
        this.ngZone.run(() => {
            this.sections = data;
            if (currentSectionId) {
              this.updateActiveSection(currentSectionId);
              this.loadExercisesForSection(currentSectionId);
            }
            this.cd.detectChanges();
        });
      });
  }

  updateActiveSection(sectionId: string) {
    const section = this.sections.find(s => s.id.toString() === sectionId.toString());
    if (section) {
      this.activeSection = section;
    }
  }

  loadExercisesForSection(id: string) {
    this.http.get<any[]>(`http://localhost:8081/exercises/section/${id}`)
      .subscribe({
        next: (data) => {
          this.ngZone.run(() => {
              this.exercises = data.map(ex => ({
                ...ex,
                parsedContent: typeof ex.content === 'string' ? JSON.parse(ex.content) : ex.content,
                userAnswers: {}
              }));
              this.cd.detectChanges();
              this.loadSectionProgress(id); // <--- Apel nou pentru Redis
          });
        },
        error: (err) => console.error("Error loading exercises:", err)
      });
  }


  getObjectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  renderTextWithGaps(text: string): SafeHtml {
    if (!text) return '';
    const replaced = text.replace(/\[gap(\d+)\]/g, (match, number) => {
      return `<input type="text" class="gap-input" data-gap="${number}" placeholder="...">`;
    });
    return this.sanitizer.bypassSecurityTrustHtml(replaced);
  }

  selectGapAnswer(ex: any, gapKey: string, option: string) {
    if (!ex.userAnswers) ex.userAnswers = {};
    ex.userAnswers[gapKey] = option;
  }

  checkOption(ex: any) {
    if (!ex.userAnswer) {
      alert("Please select an answer first!");
      return;
    }
    ex.submitted = true;
  }

  submitEntireSection() {
    let totalCorrect = 0;
    this.totalInteractives = 0;

    this.exercises.forEach(ex => {
      if (ex.type === 'Text' || ex.type === 'Video') return;

      ex.submitted = true;

      if (ex.type === 'Option') {
        this.totalInteractives++;
        if (ex.userAnswer === ex.parsedContent.answer) totalCorrect++;
      }
      else if (ex.type === 'True_False') {
        this.totalInteractives++;
        if (ex.userAnswer === ex.parsedContent.answer) totalCorrect++;
      }
      else if (ex.type === 'FillGaps') {
        const keys = Object.keys(ex.parsedContent.answer_key);
        keys.forEach(key => {
          this.totalInteractives++;
          if (ex.userAnswers && ex.userAnswers[key] === ex.parsedContent.answer_key[key]) {
            totalCorrect++;
          }
        });
      }
    });

    if (this.totalInteractives > 0) {
      this.finalScore = parseFloat(((totalCorrect / this.totalInteractives) * 5).toFixed(2));
    } else {
      this.finalScore = 0;
    }

    this.isSubmitted = true;
    console.log("Scor trimis:", this.finalScore)
    this.submitProgress();

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  submitProgress() {
    const userId = localStorage.getItem('userId');
    if (!userId) return;

    const progressData = {
      userId: parseInt(userId, 10),
      courseId: parseInt(this.courseId!, 10),
      levelId: parseInt(this.levelId!, 10),
      sectionId: parseInt(this.activeSection.id, 10),
      score: this.finalScore,
      status: this.finalScore >= 3 ? 'COMPLETED' : 'IN_PROGRESS'
    };

    // Salvare in MySQL
    this.http.post('http://localhost:8081/progress', progressData).subscribe({
      next: (res) => {
        console.log("Progress saved:", res);
        this.saveToRedis(progressData.status); // <--- Salvare în Redis sincronizată
        const username = localStorage.getItem('username');
        if (username) {
          this.authService.fetchUserInfo(username);
        }
      },
      error: (err) => console.error("Failed to save progress:", err)
    });
  }

  // --- METODE ADAUGATE PENTRU REDIS ---

  loadSectionProgress(sectionId: string) {
    const userId = localStorage.getItem('userId');
    if (!userId) return;

    this.http.get<any>(`http://localhost:8081/redis-progress/section`, {
      params: {
        userId: userId,
        courseId: this.courseId!,
        sectionId: sectionId
      }
    }).subscribe(progress => {
      if (progress && progress.userId) {
        this.applyProgress(progress);
      }
    });
  }

  applyProgress(progress: any) {
    this.finalScore = progress.finalScore ?? 0;
    this.isSubmitted = progress.status === 'COMPLETED';

    if (progress.exercises && Array.isArray(progress.exercises)) {
      this.exercises.forEach(ex => {
        const saved = progress.exercises.find((s: any) => s.exerciseId === ex.id);
        if (saved) {
          ex.userAnswer = saved.userAnswer;
          ex.userAnswers = saved.userAnswers;
          ex.submitted = saved.submitted;
        }
      });
    }
    this.cd.detectChanges();
  }

  saveToRedis(status: string) {
    const userId = localStorage.getItem('userId');
    if (!userId || !this.activeSection) return;

    const redisData = {
      userId: parseInt(userId, 10),
      courseId: parseInt(this.courseId!, 10),
      sectionId: parseInt(this.activeSection.id, 10),
      levelId: parseInt(this.levelId!, 10),
      finalScore: this.finalScore,
      status: status,
      exercises: this.exercises.map(ex => ({
        exerciseId: ex.id,
        userAnswer: ex.userAnswer,
        userAnswers: ex.userAnswers,
        submitted: ex.submitted
      }))
    };

    this.http.post('http://localhost:8081/redis-progress/section', redisData).subscribe();
  }
}
