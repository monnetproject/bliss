#!/usr/bin/perl

my $title = $ARGV[0];

if($title eq "") {
  die "Need a title";
}

while(<STDIN>) {
  my $line = $_;
  if($line =~ /.*<title>$title<\/title>.*/) {
    print "<page>\n<title>$title</title>\n";
    last;
  }
}

while(<STDIN>) {
  print $_;
}
